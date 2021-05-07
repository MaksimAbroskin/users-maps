package ru.dins.scalaschool.file_to_map

//import io.circe.Json
//import io.circe.literal.JsonStringContext
//import io.circe.syntax.EncoderOps

object Main extends App {
//  val test: Json = io.circe.parser.parse("""{
//  "response" : {
//    "GeoObjectCollection" : {
//    "metaDataProperty" : {
//    "GeocoderResponseMetaData" : {
//    "request" : "Невский 50",
//    "results" : "1",
//    "found" : "1"
//  }
//  },
//    "featureMember" : [
//  {
//    "GeoObject" : {
//    "metaDataProperty" : {
//    "GeocoderMetaData" : {
//    "precision" : "exact",
//    "text" : "Россия, Санкт-Петербург, Невский проспект, 50",
//    "kind" : "house",
//    "Address" : {
//    "country_code" : "RU",
//    "formatted" : "Россия, Санкт-Петербург, Невский проспект, 50",
//    "Components" : [
//  {
//    "kind" : "country",
//    "name" : "Россия"
//  },
//  {
//    "kind" : "province",
//    "name" : "Северо-Западный федеральный округ"
//  },
//  {
//    "kind" : "province",
//    "name" : "Санкт-Петербург"
//  },
//  {
//    "kind" : "locality",
//    "name" : "Санкт-Петербург"
//  },
//  {
//    "kind" : "street",
//    "name" : "Невский проспект"
//  },
//  {
//    "kind" : "house",
//    "name" : "50"
//  }
//    ]
//  },
//    "AddressDetails" : {
//    "Country" : {
//    "AddressLine" : "Россия, Санкт-Петербург, Невский проспект, 50",
//    "CountryNameCode" : "RU",
//    "CountryName" : "Россия",
//    "AdministrativeArea" : {
//    "AdministrativeAreaName" : "Санкт-Петербург",
//    "Locality" : {
//    "LocalityName" : "Санкт-Петербург",
//    "Thoroughfare" : {
//    "ThoroughfareName" : "Невский проспект",
//    "Premise" : {
//    "PremiseNumber" : "50"
//  }
//  }
//  }
//  }
//  }
//  }
//  }
//  },
//    "name" : "Невский проспект, 50",
//    "description" : "Санкт-Петербург, Россия",
//    "boundedBy" : {
//    "Envelope" : {
//    "lowerCorner" : "30.330681 59.932545",
//    "upperCorner" : "30.338892 59.936665"
//  }
//  },
//    "Point" : {
//    "pos" : "30.334787 59.934605"
//  }
//  }
//  }
//    ]
//  }
//  }
//}""").getOrElse(Json.Null)
//  println(test.as[YandPoint])
}
