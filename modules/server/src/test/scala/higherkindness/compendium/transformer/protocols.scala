/*
 * Copyright 2018-2020 47 Degrees, LLC. <http://www.47deg.com>
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

package higherkindness.compendium.transformer

object protocols {

  val simpleAvroExample: String =
    """
{
  "namespace": "example.avro",
  "protocol": "sample",
  "type": "record",
  "name": "User",
  "fields": [
    {
      "name": "name",
      "type": "string"
    },
    {
      "name": "favorite_number",
      "type": [
        "int",
        "null"
      ]
    },
    {
      "name": "favorite_color",
      "type": [
        "string",
        "null"
      ]
    }
  ]
}
  """

  val simpleProtobufExample: String = """
   syntax = "proto3";
   package com.acme;

   message Book {
       reserved 4, 8;
       reserved 12 to 15;
       int64 isbn = 1;
       string title = 2;
       BindingType binding_type = 9;
   }
                                        
   message GetBookRequest {
       int64 isbn = 1;
   }

                                        
    message BookStore {
        string name = 1;
        map<int64, string> books = 2;
        repeated Genre genres = 3;
                                        
        oneof payment_method {
            int64 credit_card_number = 4;
            int32 cash = 5;
            string iou_note = 6;
            Book barter = 7;
        }
    }
                                        
    enum Genre {
        option allow_alias = true;
        UNKNOWN = 0;
        SCIENCE_FICTION = 1;
        SPECULATIVE_FICTION = 1;
        POETRY = 2;
        SCI_FI = 1;
    }
                                        
    enum BindingType {
        HARDCOVER = 0;
        PAPERBACK = 1;
    }

    service BookService {
        rpc GetBook (GetBookRequest) returns (Book) {}
        rpc GetGreatestBook (stream GetBookRequest) returns (Book) {}
        rpc GetBooks (stream GetBookRequest) returns (stream Book) {}
    }
"""

}
