/*
 * Copyright (C) 2025  Linked Ideal LLC.[https://linked-ideal.com/]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package controllers


import com.ideal.linked.toposoid.common.{CLAIM, PREMISE, TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.model.{KnowledgeBaseEdge, KnowledgeBaseNode, KnowledgeBaseSemiGlobalNode, KnowledgeFeatureReference, LocalContext, LocalContextForFeature, PredicateArgumentStructure}
import com.ideal.linked.toposoid.knowledgebase.nlp.model.{SingleSentence, SurfaceInfo}
import com.ideal.linked.toposoid.knowledgebase.regist.model.Knowledge
import com.ideal.linked.toposoid.protocol.model.base.{AnalyzedSentenceObject, AnalyzedSentenceObjects, CoveredPropositionResult, DeductionResult}
import com.ideal.linked.toposoid.protocol.model.parser.{InputSentenceForParser, KnowledgeForParser}
import com.ideal.linked.toposoid.sentence.parser.japanese.SentenceParser
import com.typesafe.scalalogging.LazyLogging

import javax.inject._
import play.api._
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc._

import scala.util.matching.Regex
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
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE .str).get).as[TransversalState]
    try {
      val json = request.body
      val inputSentenceForParser: InputSentenceForParser = Json.parse(json.toString).as[InputSentenceForParser]
      logger.info(ToposoidUtils.formatMessageForLogger("PREMISE:" + inputSentenceForParser.premise.map(_.knowledge.sentence).mkString(","), transversalState.userId))
      logger.info(ToposoidUtils.formatMessageForLogger("CLAIM:" + inputSentenceForParser.claim.map(_.knowledge.sentence).mkString(","), transversalState.userId))
      if(inputSentenceForParser.premise.size > 0 && inputSentenceForParser.claim.size  == 0){
        BadRequest(Json.obj("status" ->"Error", "message" -> "It is not possible to register only as a prerequisite. If you have any premises, please also register a claim."))
      }else{
        val result:AnalyzedSentenceObjects = AnalyzedSentenceObjects(this.setData(inputSentenceForParser.premise, PREMISE.index).analyzedSentenceObjects ::: this.setData(inputSentenceForParser.claim, CLAIM.index).analyzedSentenceObjects)
        logger.info(ToposoidUtils.formatMessageForLogger("Parsing completed.", transversalState.userId))
        Ok(Json.toJson(result)).as(JSON)
      }
    }catch{
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.userId), e)
        BadRequest(Json.obj("status" ->"Error", "message" -> e.toString()))
      }
    }
  }

  /**
   * input Text from frontend
   * @return the surface part of the predicate structure analysis result
   */
  def split() = Action(parse.json) { request =>
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE .str).get).as[TransversalState]
    try {
      val json = request.body
      val singleSentence:SingleSentence = Json.parse(json.toString).as[SingleSentence]
      logger.info(ToposoidUtils.formatMessageForLogger("SENTENCE:" + singleSentence.sentence, transversalState.userId))
      val knowledge:Knowledge = Knowledge(sentence = singleSentence.sentence, lang = "ja_JP", extentInfoJson = "{}", isNegativeSentence = false)
      val knowledgeForParser:List[KnowledgeForParser] = List(knowledge).map(x => KnowledgeForParser(propositionId = "", sentenceId = "", knowledge = x))
      val asos = this.setData(knowledgeForParser, CLAIM.index).analyzedSentenceObjects

      val predicateArgumentStructures:List[PredicateArgumentStructure] = asos.map(_.nodeMap.map(_._2.predicateArgumentStructure)).flatten
      val surfaceInfoList:List[SurfaceInfo] = predicateArgumentStructures.filter(x => {
        x.morphemes.filter(y => y.contains("名詞")).size > 0
      }).map(y => SurfaceInfo(y.surface, y.currentId))
      logger.info(ToposoidUtils.formatMessageForLogger("Splitting completed." + surfaceInfoList.mkString(","), transversalState.userId))
      Ok(Json.toJson(surfaceInfoList.reverse)).as(JSON)
    } catch {
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.userId), e)
        BadRequest(Json.obj("status" -> "Error", "message" -> e.toString()))
      }
    }
  }
  /*
  private def parseNoReferenceSentence(knowledgeForParser:KnowledgeForParser):AnalyzedSentenceObject = {
    val propositionId = knowledgeForParser.propositionId
    val sentenceId = knowledgeForParser.sentenceId
    val documentId = knowledgeForParser.knowledge.knowledgeForDocument.id

    val localContext = LocalContext(
      lang = knowledgeForParser.knowledge.lang,
      namedEntity = "",
      rangeExpressions = Map.empty[String, Map[String, String]],
      categories = Map.empty[String, String],
      domains = Map.empty[String, String],
      knowledgeFeatureReferences = List.empty[KnowledgeFeatureReference]
    )

    val caseType = knowledgeForParser.knowledge.lang match {
      case "ja_JP" => "文末"
      case "en_US" => "ROOT"
      case _ => ""
    }

    val predicateArgumentStructure = PredicateArgumentStructure(
      currentId = 0,
      parentId = -1,
      isMainSection = true,
      surface = knowledgeForParser.knowledge.sentence,
      normalizedName = knowledgeForParser.knowledge.sentence,
      dependType = "-",
      caseType = caseType,
      isDenialWord = false,
      isConditionalConnection = false,
      surfaceYomi = "",
      normalizedNameYomi = "",
      modalityType = "-",
      parallelType = "-",
      nodeType = 1,
      morphemes = List("-")
    )

    val node = KnowledgeBaseNode(
      nodeId = sentenceId + "-0",
      propositionId = propositionId,
      sentenceId = sentenceId,
      predicateArgumentStructure = predicateArgumentStructure,
      localContext = localContext,
    )
    val nodeMap = Map(sentenceId + "-0" -> node)

    val localContextForFeature = LocalContextForFeature(
      lang = knowledgeForParser.knowledge.lang,
      knowledgeFeatureReferences = List.empty[KnowledgeFeatureReference]
    )

    val knowledgeBaseSemiGlobalNode = KnowledgeBaseSemiGlobalNode(
      sentenceId = sentenceId,
      propositionId = propositionId,
      documentId = documentId,
      sentence = knowledgeForParser.knowledge.sentence,
      sentenceType = 1,
      localContextForFeature = localContextForFeature,
    )

    val defaultDeductionResult = DeductionResult(status = false,
      coveredPropositionResults = List.empty[CoveredPropositionResult]
    )
    AnalyzedSentenceObject(
      nodeMap = nodeMap,
      edgeList = List.empty[KnowledgeBaseEdge],
      knowledgeBaseSemiGlobalNode = knowledgeBaseSemiGlobalNode,
      deductionResult = defaultDeductionResult
    )
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
        val nodeMap: Map[String, KnowledgeBaseNode] = sentenceObject._1
        val edgeList: List[KnowledgeBaseEdge] = sentenceObject._2
        val localContextForFeature: LocalContextForFeature = LocalContextForFeature(
          knowledgeForParser.knowledge.lang,
          List.empty[KnowledgeFeatureReference]
        )
        val knowledgeBaseSemiGlobalNode: KnowledgeBaseSemiGlobalNode = KnowledgeBaseSemiGlobalNode(
          knowledgeForParser.sentenceId,
          knowledgeForParser.propositionId,
          knowledgeForParser.sentenceId,
          knowledgeForParser.knowledge.sentence,
          sentenceType,
          localContextForFeature
        )
        val deductionResult: DeductionResult = DeductionResult(false, List.empty[CoveredPropositionResult])
        asoList :+= AnalyzedSentenceObject(nodeMap, edgeList, knowledgeBaseSemiGlobalNode, deductionResult)

        /*
        val noReferenceRegex:Regex = "^(NO_REFERENCE)_.+_[0-9]+$".r
        val aso = knowledgeForParser.knowledge.sentence match {
          case noReferenceRegex(x) => {
            parseNoReferenceSentence(knowledgeForParser)
          }
          case _ => {
            val sentenceObject = SentenceParser.parse(knowledgeForParser)
            val nodeMap: Map[String, KnowledgeBaseNode] = sentenceObject._1
            val edgeList: List[KnowledgeBaseEdge] = sentenceObject._2
            val localContextForFeature: LocalContextForFeature = LocalContextForFeature(
              knowledgeForParser.knowledge.lang,
              List.empty[KnowledgeFeatureReference]
            )
            val knowledgeBaseSemiGlobalNode: KnowledgeBaseSemiGlobalNode = KnowledgeBaseSemiGlobalNode(
              knowledgeForParser.sentenceId,
              knowledgeForParser.propositionId,
              knowledgeForParser.sentenceId,
              knowledgeForParser.knowledge.sentence,
              sentenceType,
              localContextForFeature
            )
            val deductionResult: DeductionResult = DeductionResult(false, List.empty[CoveredPropositionResult])
            AnalyzedSentenceObject(nodeMap, edgeList, knowledgeBaseSemiGlobalNode, deductionResult)
          }
        }
        asoList :+= aso
        */
      }
    }
    AnalyzedSentenceObjects(asoList)
  }match {
    case Success(s) => s
    case Failure(e) => throw e
  }


}
