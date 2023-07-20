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
import com.ideal.linked.toposoid.protocol.model.base.{AnalyzedSentenceObject, AnalyzedSentenceObjects}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Play.materializer
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "HomeController POST(sentence is empty)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val jsonStr:String = """{
                             |    "premise":[],
                             |    "claim":[]
                             |}
                             |""".stripMargin
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(jsonStr))
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
      val jsonStr: String =
        """{
          |    "premise":[{"propositionId": "612bf3d6-bdb5-47b9-a3a6-185015c8c414", "sentenceId": "4a2994a1-ec7a-438b-a290-0cfb563a5170", "knowledge": {"sentence": "案ずるより産むが易し。","lang": "ja_JP", "extentInfoJson": "{}", "isNegativeSentence":false}}],
          |    "claim":[]
          |}
          |""".stripMargin
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(jsonStr))
      val result = call(controller.analyze(), fr)
      status(result) mustBe BAD_REQUEST
    }
  }

  "HomeController POST(multiple sentence in premise)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val jsonStr: String =
        """{
          |    "premise":[{"propositionId": "612bf3d6-bdb5-47b9-a3a6-185015c8c414", "sentenceId": "4a2994a1-ec7a-438b-a290-0cfb563a5170", "knowledge": {"sentence": "案ずるより産むが易し。","lang": "ja_JP", "extentInfoJson": "{}", "isNegativeSentence":false}}, {"propositionId": "612bf3d6-bdb5-47b9-a3a6-185015c8c414", "sentenceId": "4a2994a1-ec7a-438b-a290-0cfb563a5170", "knowledge": {"sentence": "失敗は成功の基","lang": "ja_JP", "extentInfoJson": "{}", "isNegativeSentence":false}}],
          |    "claim":[]
          |}
          |""".stripMargin
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(jsonStr))
      val result = call(controller.analyze(), fr)
      status(result) mustBe BAD_REQUEST
    }
  }


  "HomeController POST(single sentence in claim)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val jsonStr:String = """{
                             |    "premise":[],
                             |    "claim":[{"propositionId": "612bf3d6-bdb5-47b9-a3a6-185015c8c414", "sentenceId": "4a2994a1-ec7a-438b-a290-0cfb563a5170", "knowledge": {"sentence": "案ずるより産むが易し。","lang": "ja_JP", "extentInfoJson": "{}", "isNegativeSentence":false}}]
                             |}
                             |""".stripMargin
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(jsonStr))
      val result= call(controller.analyze(), fr)
      status(result) mustBe OK
      val jsonResult:String = contentAsJson(result).toString()
      val asos:AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(asos.analyzedSentenceObjects.size == 1)
      for(aso <- asos.analyzedSentenceObjects ){
        assert(aso.sentenceType == CLAIM.index)
        val sentence:String = aso.nodeMap.map(x => x._2.predicateArgumentStructure.currentId -> x._2).toSeq.sortBy(_._1).foldLeft("") { (acc, x) => acc + x._2.predicateArgumentStructure.surface }
        assert(sentence.equals("案ずるより産むが易し。"))
      }

    }
  }

  "HomeController POST(multiple sentence in claim)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val jsonStr: String =
        """{
          |    "premise":[],
          |    "claim":[{"propositionId": "612bf3d6-bdb5-47b9-a3a6-185015c8c414", "sentenceId": "4a2994a1-ec7a-438b-a290-0cfb563a5170", "knowledge": {"sentence": "案ずるより産むが易し。","lang": "ja_JP", "extentInfoJson": "{}", "isNegativeSentence":false}}, {"propositionId": "612bf3d6-bdb5-47b9-a3a6-185015c8c414", "sentenceId": "4a2994a1-ec7a-438b-a290-0cfb563a5170", "knowledge": {"sentence": "失敗は成功の基","lang": "ja_JP", "extentInfoJson": "{}", "isNegativeSentence":false}}]
          |}
          |""".stripMargin
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(jsonStr))
      val result = call(controller.analyze(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val asos: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(asos.analyzedSentenceObjects.size == 2)
      for ((aso, i) <- asos.analyzedSentenceObjects.zipWithIndex) {
        assert(aso.sentenceType == CLAIM.index)
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
      val jsonStr: String =
        """{
          |    "premise":[{"propositionId": "612bf3d6-bdb5-47b9-a3a6-185015c8c414", "sentenceId": "4a2994a1-ec7a-438b-a290-0cfb563a5170", "knowledge": {"sentence": "失敗は成功の基。","lang": "ja_JP", "extentInfoJson": "{}", "isNegativeSentence":false}}],
          |    "claim":[{"propositionId": "612bf3d6-bdb5-47b9-a3a6-185015c8c414", "sentenceId": "4a2994a1-ec7a-438b-a290-0cfb563a5170", "knowledge": {"sentence": "案ずるより産むが易し。","lang": "ja_JP", "extentInfoJson": "{}", "isNegativeSentence":false}}]
          |}
          |""".stripMargin
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(jsonStr))
      val result = call(controller.analyze(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val asos: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(asos.analyzedSentenceObjects.size == 2)
      for ((aso, i) <- asos.analyzedSentenceObjects.zipWithIndex) {
        val sentence: String = aso.nodeMap.map(x => x._2.predicateArgumentStructure.currentId -> x._2).toSeq.sortBy(_._1).foldLeft("") { (acc, x) => acc + x._2.predicateArgumentStructure.surface }
        if (aso.sentenceType == PREMISE.index) {
          assert(sentence.equals("失敗は成功の基。"))
        } else if (aso.sentenceType == CLAIM.index) {
          assert(sentence.equals("案ずるより産むが易し。"))
        }
      }
    }
  }

  "HomeController POST(multiple sentence in premise and multiple sentence in claim)" should {
    "returns an appropriate response" in {
      val controller: HomeController = inject[HomeController]
      val jsonStr: String =
        """{
          |    "premise":[{"propositionId": "612bf3d6-bdb5-47b9-a3a6-185015c8c414", "sentenceId": "4a2994a1-ec7a-438b-a290-0cfb563a5170", "knowledge": {"sentence": "失敗は成功の基。","lang": "ja_JP", "extentInfoJson": "{}", "isNegativeSentence":false}}, {"propositionId": "612bf3d6-bdb5-47b9-a3a6-185015c8c414", "sentenceId": "4a2994a1-ec7a-438b-a290-0cfb563a5170", "knowledge": {"sentence": "思い立ったが吉日。","lang": "ja_JP", "extentInfoJson": "{}", "isNegativeSentence":false}}],
          |    "claim":[{"propositionId": "612bf3d6-bdb5-47b9-a3a6-185015c8c414", "sentenceId": "4a2994a1-ec7a-438b-a290-0cfb563a5170", "knowledge": {"sentence": "案ずるより産むが易し。","lang": "ja_JP", "extentInfoJson": "{}", "isNegativeSentence":false}}, {"propositionId": "612bf3d6-bdb5-47b9-a3a6-185015c8c414", "sentenceId": "4a2994a1-ec7a-438b-a290-0cfb563a5170", "knowledge": {"sentence": "蓮の台の半座を分かつ。","lang": "ja_JP", "extentInfoJson": "{}", "isNegativeSentence":false}}]
          |}
          |""".stripMargin
      val fr = FakeRequest(POST, "/analyze")
        .withHeaders("Content-type" -> "application/json")
        .withJsonBody(Json.parse(jsonStr))
      val result = call(controller.analyze(), fr)
      status(result) mustBe OK
      val jsonResult: String = contentAsJson(result).toString()
      val asos: AnalyzedSentenceObjects = Json.parse(jsonResult).as[AnalyzedSentenceObjects]
      assert(asos.analyzedSentenceObjects.size == 4)
      for ((aso, i) <- asos.analyzedSentenceObjects.zipWithIndex) {
        val sentence: String = aso.nodeMap.map(x => x._2.predicateArgumentStructure.currentId -> x._2).toSeq.sortBy(_._1).foldLeft("") { (acc, x) => acc + x._2.predicateArgumentStructure.surface }
        if (aso.sentenceType == PREMISE.index) {
          if(i == 0){
            assert(sentence.equals("失敗は成功の基。"))
          }else if(i == 1){
            assert(sentence.equals("思い立ったが吉日。"))
          }
        } else if (aso.sentenceType == CLAIM.index) {
          if(i == 2){
            assert(sentence.equals("案ずるより産むが易し。"))
          }else if(i == 3){
            assert(sentence.equals("蓮の台の半座を分かつ。"))
          }
        }
      }
    }
  }
}
