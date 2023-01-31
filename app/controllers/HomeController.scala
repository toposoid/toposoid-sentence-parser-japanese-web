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
import com.ideal.linked.toposoid.knowledgebase.model.{KnowledgeBaseEdge, KnowledgeBaseNode}
import com.ideal.linked.toposoid.knowledgebase.regist.model.Knowledge
import com.ideal.linked.toposoid.protocol.model.base.{AnalyzedSentenceObject, AnalyzedSentenceObjects, DeductionResult}
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
  /*
  @deprecated
  def analyzeOneSentence()  = Action(parse.json) { request =>
    try {
      val json = request.body
      val knowledgeForParser : KnowledgeForParser = Json.parse(json.toString).as[KnowledgeForParser]
      val deductionResultMap:Map[String, DeductionResult] =
        Map(
          PREMISE.index.toString -> DeductionResult(false, List.empty[String], ""),
          CLAIM.index.toString -> DeductionResult(false, List.empty[String],"")
        )
      if(knowledgeForParser.knowledge.sentence.strip() == "") {
        val defaultAso:AnalyzedSentenceObject = AnalyzedSentenceObject(Map.empty[String, KnowledgeBaseNode], List.empty[KnowledgeBaseEdge], -1, knowledgeForParser.sentenceId, knowledgeForParser.knowledge.sentence, deductionResultMap)
        Ok(Json.toJson(defaultAso)).as(JSON)
      }else{
        val sentenceObject = SentenceParser.parse(knowledgeForParser)
        val nodeMap:Map[String, KnowledgeBaseNode] = sentenceObject._1
        val edgeList:List[KnowledgeBaseEdge] = sentenceObject._2
        val aso = AnalyzedSentenceObject(nodeMap, edgeList, -1, knowledgeForParser.sentenceId, knowledgeForParser.knowledge.sentence, deductionResultMap) //TODO:　In this case The 3rd and 4th argument is meaningless
        Ok(Json.toJson(aso)).as(JSON)
      }
    }catch{
      case e: Exception => {
        logger.error(e.toString, e)
        BadRequest(Json.obj("status" ->"Error", "message" -> e.toString()))
      }
    }
  }
  */
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
        val deductionResultMap:Map[String, DeductionResult] =
          Map(
            PREMISE.index.toString -> DeductionResult(false, List.empty[String], ""),
            CLAIM.index.toString -> DeductionResult(false, List.empty[String],"")
          )
        val aso = AnalyzedSentenceObject(nodeMap, edgeList, sentenceType, knowledgeForParser.sentenceId, knowledgeForParser.knowledge.lang, deductionResultMap)
        asoList :+= aso
      }
    }
    AnalyzedSentenceObjects(asoList)
  }match {
    case Success(s) => s
    case Failure(e) => throw e
  }


}
