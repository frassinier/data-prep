module.exports = {"request":{"url":"http://10.42.10.99:8888/api/preparations","method":"GET","path":"/api/preparations"},"response":{"data":[{"id":"8153b02287ec9d23ed5e2726901f8a0a30bac7f5","dataSetId":"49be2c65-90c5-49fa-aa9b-945625f82832","author":"anonymousUser","name":"clean cars","creationDate":1450706774383,"lastModificationDate":1450706810882,"steps":["f6e172c33bdacbc69bca9d32b2bd78174712a171","7ce1e7d443734f55f2f4925c0446ff475f4ef567","675f874cf1b368f6ecaa50ac739f38876d9902e5","09693417159d438c8e13ec712ceab584f46ff294","75e71b10bee3dfdd2f86f0f5f5d8a91096141175"],"diff":[{"createdColumns":[]},{"createdColumns":["0008","0009"]},{"createdColumns":[]},{"createdColumns":[]}],"actions":[{"action":"round","parameters":{"column_id":"0001","scope":"column","column_name":"economy (mpg)","row_id":"1"}},{"action":"split","parameters":{"column_id":"0000","scope":"column","limit":"2","column_name":"name","row_id":"1","separator":" "}},{"action":"rename_column","parameters":{"column_id":"0008","scope":"column","column_name":"name_split","new_column_name":"company"}},{"action":"rename_column","parameters":{"column_id":"0009","scope":"column","column_name":"name_split","new_column_name":"model"}}],"metadata":[{"name":"round","category":"math","description":"Round value to the closest integer (3.14 -> 3)","label":"Round Value","actionScope":[],"dynamic":false,"parameters":[{"name":"column_id","type":"string","implicit":true,"canBeBlank":true,"description":"The column to which you want to apply this action","label":"Column","default":""},{"name":"row_id","type":"string","implicit":true,"canBeBlank":true,"description":"The row to which you want to apply this action","label":"Row","default":""},{"name":"scope","type":"string","implicit":true,"canBeBlank":true,"description":"The transformation scope (CELL | LINE | COLUMN | DATASET)","label":"Scope","default":""},{"name":"filter","type":"filter","implicit":true,"canBeBlank":true,"description":"An optional filter to apply action on matching values only.","label":"Filter","default":""}]},{"category":"split","name":"split","parameters":[{"name":"column_id","type":"string","implicit":true,"canBeBlank":true,"description":"The column to which you want to apply this action","label":"Column","default":""},{"name":"row_id","type":"string","implicit":true,"canBeBlank":true,"description":"The row to which you want to apply this action","label":"Row","default":""},{"name":"scope","type":"string","implicit":true,"canBeBlank":true,"description":"The transformation scope (CELL | LINE | COLUMN | DATASET)","label":"Scope","default":""},{"name":"filter","type":"filter","implicit":true,"canBeBlank":true,"description":"An optional filter to apply action on matching values only.","label":"Filter","default":""},{"name":"limit","type":"integer","implicit":false,"canBeBlank":true,"description":"Number of Parts to split the value in","label":"Parts","default":"2"},{"name":"separator","type":"select","implicit":false,"canBeBlank":false,"configuration":{"values":[{"value":":","label":":"},{"value":";","label":";"},{"value":",","label":","},{"value":"@","label":"@"},{"value":"-","label":"-"},{"value":"_","label":"_"},{"value":" ","label":"<space>"},{"value":"\t","label":"<tab>"},{"value":"other (string)","label":"other (string)","parameters":[{"name":"manual_separator_string","type":"string","implicit":false,"canBeBlank":true,"description":"Choose your own separator","label":"Manual separator","default":""}]},{"value":"other (regex)","label":"other (regex)","parameters":[{"name":"manual_separator_regex","type":"string","implicit":false,"canBeBlank":true,"description":"Choose your own separator","label":"Manual separator","default":""}]}],"multiple":false},"description":"Character, litteral or regex to use as separator","label":"Separator","default":":"}],"description":"Split column from separators","label":"Split the Text in Parts","actionScope":[],"dynamic":false},{"parameters":[{"name":"column_id","type":"string","implicit":true,"canBeBlank":true,"description":"The column to which you want to apply this action","label":"Column","default":""},{"name":"row_id","type":"string","implicit":true,"canBeBlank":true,"description":"The row to which you want to apply this action","label":"Row","default":""},{"name":"scope","type":"string","implicit":true,"canBeBlank":true,"description":"The transformation scope (CELL | LINE | COLUMN | DATASET)","label":"Scope","default":""},{"name":"filter","type":"filter","implicit":true,"canBeBlank":true,"description":"An optional filter to apply action on matching values only.","label":"Filter","default":""},{"name":"new_column_name","type":"string","implicit":false,"canBeBlank":false,"description":"The new column name","label":"New name","default":""}],"category":"column_metadata","actionScope":["column_metadata"],"name":"rename_column","description":"Rename this column","label":"Rename Column","dynamic":false},{"parameters":[{"name":"column_id","type":"string","implicit":true,"canBeBlank":true,"description":"The column to which you want to apply this action","label":"Column","default":""},{"name":"row_id","type":"string","implicit":true,"canBeBlank":true,"description":"The row to which you want to apply this action","label":"Row","default":""},{"name":"scope","type":"string","implicit":true,"canBeBlank":true,"description":"The transformation scope (CELL | LINE | COLUMN | DATASET)","label":"Scope","default":""},{"name":"filter","type":"filter","implicit":true,"canBeBlank":true,"description":"An optional filter to apply action on matching values only.","label":"Filter","default":""},{"name":"new_column_name","type":"string","implicit":false,"canBeBlank":false,"description":"The new column name","label":"New name","default":""}],"category":"column_metadata","actionScope":["column_metadata"],"name":"rename_column","description":"Rename this column","label":"Rename Column","dynamic":false}]},{"id":"cba9b67aeeb014f9f37f377be18b4aba5c75d312","dataSetId":"6dc541ce-23fe-4c49-86d1-cf88355bfd5c","author":"anonymousUser","name":"clean client","creationDate":1450706823368,"lastModificationDate":1450706931540,"steps":["f6e172c33bdacbc69bca9d32b2bd78174712a171","3723a1eaf9ecf6e4c139312dae8889473a6e01d8","93b111df43b55c4001b1461a20662c3b7214eca5","8ca0313127b0006671871a14609e7d18c94333ac","1e54ea7f07296d8ee23dab3ef05b0bd5e476ccff","d9be120c9c8723eab1c22d96ede645df39e59b68"],"diff":[{"createdColumns":[]},{"createdColumns":[]},{"createdColumns":[]},{"createdColumns":[]},{"createdColumns":[]}],"actions":[{"action":"propercase","parameters":{"column_id":"0002","scope":"column","column_name":"last_name","row_id":"7"}},{"action":"type_change","parameters":{"column_id":"0002","new_type":"STRING","scope":"column","column_name":"last_name"}},{"action":"remove_non_num_chars","parameters":{"column_id":"0000","scope":"column","column_name":"id","row_id":"1"}},{"action":"replace_on_value","parameters":{"column_id":"0003","scope":"cell","column_name":"email","cell_value":{"token":"gbr!ownr@canalblog.com","operator":"equals"},"row_id":"28","replace_value":"gbrownr@canalblog.com"}},{"action":"replace_on_value","parameters":{"column_id":"0003","scope":"cell","column_name":"email","cell_value":{"token":"kgarcia14@goo,gl","operator":"equals"},"row_id":"41","replace_value":"kgarcia14@google.com"}}],"metadata":[{"category":"strings","name":"propercase","description":"Converts the text content from this column to title case (i.e. \"data prep\" -> \"Data Prep\")","label":"Change Style to Title Case","actionScope":[],"dynamic":false,"parameters":[{"name":"column_id","type":"string","implicit":true,"canBeBlank":true,"description":"The column to which you want to apply this action","label":"Column","default":""},{"name":"row_id","type":"string","implicit":true,"canBeBlank":true,"description":"The row to which you want to apply this action","label":"Row","default":""},{"name":"scope","type":"string","implicit":true,"canBeBlank":true,"description":"The transformation scope (CELL | LINE | COLUMN | DATASET)","label":"Scope","default":""},{"name":"filter","type":"filter","implicit":true,"canBeBlank":true,"description":"An optional filter to apply action on matching values only.","label":"Filter","default":""}]},{"category":"column_metadata","name":"type_change","description":"Change type of this column (number, text, date, etc.)","label":"Change Data Type","actionScope":[],"dynamic":false,"parameters":[{"name":"column_id","type":"string","implicit":true,"canBeBlank":true,"description":"The column to which you want to apply this action","label":"Column","default":""},{"name":"row_id","type":"string","implicit":true,"canBeBlank":true,"description":"The row to which you want to apply this action","label":"Row","default":""},{"name":"scope","type":"string","implicit":true,"canBeBlank":true,"description":"The transformation scope (CELL | LINE | COLUMN | DATASET)","label":"Scope","default":""},{"name":"filter","type":"filter","implicit":true,"canBeBlank":true,"description":"An optional filter to apply action on matching values only.","label":"Filter","default":""}]},{"category":"strings advanced","name":"remove_non_num_chars","description":"For example Ã¢âÂ¬10.5k will become 10.5","label":"Remove all non Numeric Characters","actionScope":[],"dynamic":false,"parameters":[{"name":"column_id","type":"string","implicit":true,"canBeBlank":true,"description":"The column to which you want to apply this action","label":"Column","default":""},{"name":"row_id","type":"string","implicit":true,"canBeBlank":true,"description":"The row to which you want to apply this action","label":"Row","default":""},{"name":"scope","type":"string","implicit":true,"canBeBlank":true,"description":"The transformation scope (CELL | LINE | COLUMN | DATASET)","label":"Scope","default":""},{"name":"filter","type":"filter","implicit":true,"canBeBlank":true,"description":"An optional filter to apply action on matching values only.","label":"Filter","default":""}]},{"category":"strings","name":"replace_on_value","parameters":[{"name":"column_id","type":"string","implicit":true,"canBeBlank":true,"description":"The column to which you want to apply this action","label":"Column","default":""},{"name":"row_id","type":"string","implicit":true,"canBeBlank":true,"description":"The row to which you want to apply this action","label":"Row","default":""},{"name":"scope","type":"string","implicit":true,"canBeBlank":true,"description":"The transformation scope (CELL | LINE | COLUMN | DATASET)","label":"Scope","default":""},{"name":"filter","type":"filter","implicit":true,"canBeBlank":true,"description":"An optional filter to apply action on matching values only.","label":"Filter","default":""},{"name":"cell_value","type":"regex","implicit":false,"canBeBlank":true,"description":"The current cell value","label":"Current","default":""},{"name":"replace_value","type":"string","implicit":false,"canBeBlank":true,"description":"The new value to set","label":"Replace","default":""},{"name":"replace_entire_cell","type":"boolean","implicit":false,"canBeBlank":true,"description":"Defines greedy level. If checked, it will replace the entire cell, if not, it will only replace the part of the value that matches.","label":"Replace entire cell","default":"false"}],"description":"Replace the cells that have a specific value","label":"Replace the Cells that Match","actionScope":[],"dynamic":false},{"category":"strings","name":"replace_on_value","parameters":[{"name":"column_id","type":"string","implicit":true,"canBeBlank":true,"description":"The column to which you want to apply this action","label":"Column","default":""},{"name":"row_id","type":"string","implicit":true,"canBeBlank":true,"description":"The row to which you want to apply this action","label":"Row","default":""},{"name":"scope","type":"string","implicit":true,"canBeBlank":true,"description":"The transformation scope (CELL | LINE | COLUMN | DATASET)","label":"Scope","default":""},{"name":"filter","type":"filter","implicit":true,"canBeBlank":true,"description":"An optional filter to apply action on matching values only.","label":"Filter","default":""},{"name":"cell_value","type":"regex","implicit":false,"canBeBlank":true,"description":"The current cell value","label":"Current","default":""},{"name":"replace_value","type":"string","implicit":false,"canBeBlank":true,"description":"The new value to set","label":"Replace","default":""},{"name":"replace_entire_cell","type":"boolean","implicit":false,"canBeBlank":true,"description":"Defines greedy level. If checked, it will replace the entire cell, if not, it will only replace the part of the value that matches.","label":"Replace entire cell","default":"false"}],"description":"Replace the cells that have a specific value","label":"Replace the Cells that Match","actionScope":[],"dynamic":false}]}]}}