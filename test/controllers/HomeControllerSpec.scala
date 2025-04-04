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

import com.ideal.linked.toposoid.common.{CLAIM, PREMISE, TRANSVERSAL_STATE, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.regist.model.Knowledge
import com.ideal.linked.toposoid.protocol.model.base.AnalyzedSentenceObjects
import com.ideal.linked.toposoid.protocol.model.parser.{InputSentenceForParser, KnowledgeForParser}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Play.materializer
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._
import io.jvm.uuid.UUID

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val transversalState:String = Json.toJson(TransversalState(userId="test-user", username="guest", roleId=0, csrfToken = "")).toString()



  "HomeController POST(sentence is empty)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val input = InputSentenceForParser(List.empty[KnowledgeForParser], List.empty[KnowledgeForParser])
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
        .withJsonBody(Json.toJson(input))
      val result= call(controller.analyze(), fr)
      status(result) mustBe OK
      val jsonResult:String = contentAsJson(result).toString()
      val asos:AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(asos.analyzedSentenceObjects.size == 0)
    }
  }

  "HomeController POST(single sentence in premise)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val knowledge1 = Knowledge(sentence = "案ずるより産むが易し。", lang = "ja_JP", extentInfoJson = "{}")
      val premise1 = KnowledgeForParser(propositionId = UUID.random.toString , sentenceId = UUID.random.toString, knowledge = knowledge1)
      val input = InputSentenceForParser(List(premise1), List.empty[KnowledgeForParser])
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
        .withJsonBody(Json.toJson(input))
      val result = call(controller.analyze(), fr)
      status(result) mustBe BAD_REQUEST
    }
  }

  "HomeController POST(multiple sentence in premise)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val knowledge1 = Knowledge(sentence = "案ずるより産むが易し。", lang = "ja_JP", extentInfoJson = "{}")
      val knowledge2 = Knowledge(sentence = "失敗は成功の基。", lang = "ja_JP", extentInfoJson = "{}")
      val premise1 = KnowledgeForParser(propositionId = UUID.random.toString, sentenceId = UUID.random.toString, knowledge = knowledge1)
      val premise2 = KnowledgeForParser(propositionId = UUID.random.toString, sentenceId = UUID.random.toString, knowledge = knowledge2)
      val input = InputSentenceForParser(List(premise1, premise2), List.empty[KnowledgeForParser])
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
        .withJsonBody(Json.toJson(input))
      val result = call(controller.analyze(), fr)
      status(result) mustBe BAD_REQUEST
    }
  }


  "HomeController POST(single sentence in claim)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val knowledge1 = Knowledge(sentence = "案ずるより産むが易し。", lang = "ja_JP", extentInfoJson = "{}")
      val claim1 = KnowledgeForParser(propositionId = UUID.random.toString, sentenceId = UUID.random.toString, knowledge = knowledge1)
      val input = InputSentenceForParser(List.empty[KnowledgeForParser], List(claim1))
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
        .withJsonBody(Json.toJson(input))
      val result= call(controller.analyze(), fr)
      status(result) mustBe OK
      val jsonResult:String = contentAsJson(result).toString()
      val asos:AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(asos.analyzedSentenceObjects.size == 1)
      for(aso <- asos.analyzedSentenceObjects ){
        assert(aso.knowledgeBaseSemiGlobalNode.sentenceType == CLAIM.index)
        val sentence:String = aso.nodeMap.map(x => x._2.predicateArgumentStructure.currentId -> x._2).toSeq.sortBy(_._1).foldLeft("") { (acc, x) => acc + x._2.predicateArgumentStructure.surface }
        assert(sentence.equals("案ずるより産むが易し。"))
      }

    }
  }

  "HomeController POST(multiple sentence in claim)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val knowledge1 = Knowledge(sentence = "案ずるより産むが易し。", lang = "ja_JP", extentInfoJson = "{}")
      val knowledge2 = Knowledge(sentence = "失敗は成功の基", lang = "ja_JP", extentInfoJson = "{}")
      val claim1 = KnowledgeForParser(propositionId = UUID.random.toString, sentenceId = UUID.random.toString, knowledge = knowledge1)
      val claim2 = KnowledgeForParser(propositionId = UUID.random.toString, sentenceId = UUID.random.toString, knowledge = knowledge2)
      val input = InputSentenceForParser(List.empty[KnowledgeForParser], List(claim1, claim2))
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
        .withJsonBody(Json.toJson(input))
      val result = call(controller.analyze(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val asos: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(asos.analyzedSentenceObjects.size == 2)
      for ((aso, i) <- asos.analyzedSentenceObjects.zipWithIndex) {
        assert(aso.knowledgeBaseSemiGlobalNode.sentenceType == CLAIM.index)
        val sentence: String = aso.nodeMap.map(x => x._2.predicateArgumentStructure.currentId -> x._2).toSeq.sortBy(_._1).foldLeft("") { (acc, x) => acc + x._2.predicateArgumentStructure.surface }
        if (i == 0) {
          assert(sentence.equals("案ずるより産むが易し。"))
        } else {
          assert(sentence.equals("失敗は成功の基"))
        }
      }
    }
  }

  "HomeController POST(simple sentence in premise and simple sentence in claim)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val knowledge1 = Knowledge(sentence = "失敗は成功の基。", lang = "ja_JP", extentInfoJson = "{}")
      val knowledge2 = Knowledge(sentence = "案ずるより産むが易し。", lang = "ja_JP", extentInfoJson = "{}")
      val premise1 = KnowledgeForParser(propositionId = UUID.random.toString, sentenceId = UUID.random.toString, knowledge = knowledge1)
      val claim1 = KnowledgeForParser(propositionId = UUID.random.toString, sentenceId = UUID.random.toString, knowledge = knowledge2)
      val input = InputSentenceForParser(List(premise1), List(claim1))
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
        .withJsonBody(Json.toJson(input))
      val result = call(controller.analyze(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val asos: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(asos.analyzedSentenceObjects.size == 2)
      for ((aso, i) <- asos.analyzedSentenceObjects.zipWithIndex) {
        val sentence: String = aso.nodeMap.map(x => x._2.predicateArgumentStructure.currentId -> x._2).toSeq.sortBy(_._1).foldLeft("") { (acc, x) => acc + x._2.predicateArgumentStructure.surface }
        if (aso.knowledgeBaseSemiGlobalNode.sentenceType == PREMISE.index) {
          assert(sentence.equals("失敗は成功の基。"))
        } else if (aso.knowledgeBaseSemiGlobalNode.sentenceType == CLAIM.index) {
          assert(sentence.equals("案ずるより産むが易し。"))
        }
      }
    }
  }

  "HomeController POST(multiple sentence in premise and multiple sentence in claim)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val knowledge1 = Knowledge(sentence = "失敗は成功の基。", lang = "ja_JP", extentInfoJson = "{}")
      val knowledge2 = Knowledge(sentence = "思い立ったが吉日。", lang = "ja_JP", extentInfoJson = "{}")
      val knowledge3 = Knowledge(sentence = "案ずるより産むが易し。", lang = "ja_JP", extentInfoJson = "{}")
      val knowledge4 = Knowledge(sentence = "蓮の台の半座を分かつ。", lang = "ja_JP", extentInfoJson = "{}")

      val premise1 = KnowledgeForParser(propositionId = UUID.random.toString, sentenceId = UUID.random.toString, knowledge = knowledge1)
      val premise2 = KnowledgeForParser(propositionId = UUID.random.toString, sentenceId = UUID.random.toString, knowledge = knowledge2)
      val claim1 = KnowledgeForParser(propositionId = UUID.random.toString, sentenceId = UUID.random.toString, knowledge = knowledge3)
      val claim2 = KnowledgeForParser(propositionId = UUID.random.toString, sentenceId = UUID.random.toString, knowledge = knowledge4)
      val input = InputSentenceForParser(List(premise1, premise2), List(claim1, claim2))
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
        .withJsonBody(Json.toJson(input))

      val result = call(controller.analyze(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val asos: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(asos.analyzedSentenceObjects.size == 4)
      for ((aso, i) <- asos.analyzedSentenceObjects.zipWithIndex) {
        val sentence: String = aso.nodeMap.map(x => x._2.predicateArgumentStructure.currentId -> x._2).toSeq.sortBy(_._1).foldLeft("") { (acc, x) => acc + x._2.predicateArgumentStructure.surface }
        if (aso.knowledgeBaseSemiGlobalNode.sentenceType == PREMISE.index) {
          if(i == 0){
            assert(sentence.equals("失敗は成功の基。"))
          }else if(i == 1){
            assert(sentence.equals("思い立ったが吉日。"))
          }
        } else if (aso.knowledgeBaseSemiGlobalNode.sentenceType == CLAIM.index) {
          if(i == 2){
            assert(sentence.equals("案ずるより産むが易し。"))
          }else if(i == 3){
            assert(sentence.equals("蓮の台の半座を分かつ。"))
          }
        }
      }
    }
  }

  "HomeController POST(no-reference-sentence)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val testSentence = "NO_REFERENCE_" + UUID.random.toString + "_1"
      val knowledge1 = Knowledge(sentence = testSentence, lang = "ja_JP", extentInfoJson = "{}")
      val claim1 = KnowledgeForParser(propositionId = UUID.random.toString, sentenceId = UUID.random.toString, knowledge = knowledge1)
      val input = InputSentenceForParser(List.empty[KnowledgeForParser], List(claim1))
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
        .withJsonBody(Json.toJson(input))
      val result = call(controller.analyze(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val asos: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(asos.analyzedSentenceObjects.size == 1)
      for (aso <- asos.analyzedSentenceObjects) {
        assert(aso.knowledgeBaseSemiGlobalNode.sentenceType == CLAIM.index)
        val sentence: String = aso.nodeMap.map(x => x._2.predicateArgumentStructure.currentId -> x._2).toSeq.sortBy(_._1).foldLeft("") { (acc, x) => acc + x._2.predicateArgumentStructure.surface }
        assert(sentence.equals(testSentence))
      }

    }
  }


  "HomeController POST(split)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val jsonStr: String =
        """{
          |    "sentence": "富士山は、2013年に世界遺産に登録された。"
          |}
          |""".stripMargin
      val fr = FakeRequest(POST, "/split")
        .withHeaders("Content-type" -> "application/json", TRANSVERSAL_STATE.str -> transversalState)
        .withJsonBody(Json.parse(jsonStr))
      val result = call(controller.split(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val correctJson = """[{"surface":"富士山は、","index":0},{"surface":"２０１３年に","index":1},{"surface":"世界遺産に","index":2},{"surface":"登録された。","index":3}]"""
      assert(jsonResult.equals(correctJson))

    }
  }

}
