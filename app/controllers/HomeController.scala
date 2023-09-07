/*
 * Copyright 2021 Linked Ideal LLC.[https://linked-ideal.com/]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers


import com.ideal.linked.toposoid.common.{CLAIM, PREMISE}
import com.ideal.linked.toposoid.knowledgebase.model.{KnowledgeBaseEdge, KnowledgeBaseNode, KnowledgeFeatureNode, KnowledgeFeatureReference, LocalContextForFeature}
import com.ideal.linked.toposoid.knowledgebase.regist.model.Knowledge
import com.ideal.linked.toposoid.protocol.model.base.{AnalyzedSentenceObject, AnalyzedSentenceObjects, DeductionResult, MatchedPropositionInfo}
import com.ideal.linked.toposoid.protocol.model.parser.{InputSentence, InputSentenceForParser, KnowledgeForParser}
import com.ideal.linked.toposoid.sentence.parser.japanese.SentenceParser
import com.typesafe.scalalogging.LazyLogging

import javax.inject._
import play.api._
import play.api.libs.json.Json
import play.api.mvc._

import scala.util.{Failure, Success, Try}

/**
 * This controller creates an `Action` to analyzes the predicate argument structure of Japanese natural sentences.
 * @param controllerComponents
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController  with LazyLogging{
  /**
   * With json as inputWhen a Japanese natural sentence is requested, the result of predicate argument structure analysis is output as Json.
   * @return
   */
  def analyze()  = Action(parse.json) { request =>
    try {
      val json = request.body
      val inputSentenceForParser: InputSentenceForParser = Json.parse(json.toString).as[InputSentenceForParser]
      logger.info(inputSentenceForParser.premise.map(_.knowledge.sentence).mkString(","))
      logger.info(inputSentenceForParser.claim.map(_.knowledge.sentence).mkString(","))
      if(inputSentenceForParser.premise.size > 0 && inputSentenceForParser.claim.size  == 0){
        BadRequest(Json.obj("status" ->"Error", "message" -> "It is not possible to register only as a prerequisite. If you have any premises, please also register a claim."))
      }else{
        val result:AnalyzedSentenceObjects = AnalyzedSentenceObjects(this.setData(inputSentenceForParser.premise, PREMISE.index).analyzedSentenceObjects ::: this.setData(inputSentenceForParser.claim, CLAIM.index).analyzedSentenceObjects)
        Ok(Json.toJson(result)).as(JSON)
      }
    }catch{
      case e: Exception => {
        logger.error(e.toString, e)
        BadRequest(Json.obj("status" ->"Error", "message" -> e.toString()))
      }
    }
  }

  /**
   * This function sets the result of predicate argument structure analysis to the AnalyzedSentenceObjects type.
   * @param sentences
   * @param sentenceType
   * @return
   */
  private def setData(knowledgeForParserList:List[KnowledgeForParser], sentenceType:Int):AnalyzedSentenceObjects = Try{
    var asoList = List.empty[AnalyzedSentenceObject]
    for((knowledgeForParser, i) <- knowledgeForParserList.zipWithIndex){
      if (knowledgeForParser.knowledge.sentence != "") {
        val sentenceObject = SentenceParser.parse(knowledgeForParser)
        val nodeMap:Map[String, KnowledgeBaseNode] = sentenceObject._1
        val edgeList:List[KnowledgeBaseEdge] = sentenceObject._2
        val localContextForFeature:LocalContextForFeature = LocalContextForFeature(
          knowledgeForParser.knowledge.lang,
          List.empty[KnowledgeFeatureReference]
        )
        val knowledgeFeatureNode:KnowledgeFeatureNode = KnowledgeFeatureNode(
          knowledgeForParser.sentenceId,
          knowledgeForParser.propositionId,
          knowledgeForParser.sentenceId,
          knowledgeForParser.knowledge.sentence,
          sentenceType,
          localContextForFeature
        )
        val deductionResultMap:Map[String, DeductionResult] =
          Map(
            PREMISE.index.toString -> DeductionResult(false, List.empty[MatchedPropositionInfo], ""),
            CLAIM.index.toString -> DeductionResult(false, List.empty[MatchedPropositionInfo],"")
          )
        val aso = AnalyzedSentenceObject(nodeMap, edgeList, knowledgeFeatureNode, deductionResultMap)
        asoList :+= aso
      }
    }
    AnalyzedSentenceObjects(asoList)
  }match {
    case Success(s) => s
    case Failure(e) => throw e
  }


}
