module.exports = {"request":{"url":"http://10.42.10.99:8888/api/export/formats","method":"GET","path":"/api/export/formats"},"response":{"data":[{"mimeType":"text/csv","extension":".csv","id":"CSV","needParameters":"true","defaultExport":"false","parameters":[{"name":"csvSeparator","labelKey":"CHOOSE_SEPARATOR","type":"radio","defaultValue":{"value":";","labelKey":"SEPARATOR_SEMI_COLON"},"values":[{"value":"\t","labelKey":"SEPARATOR_TAB"},{"value":" ","labelKey":"SEPARATOR_SPACE"},{"value":",","labelKey":"SEPARATOR_COMMA"}]},{"name":"fileName","labelKey":"EXPORT_FILENAME","type":"text","defaultValue":{"value":"","labelKey":"EXPORT_FILENAME_DEFAULT"}}]},{"mimeType":"application/vnd.ms-excel","extension":".xlsx","id":"XLSX","needParameters":"true","defaultExport":"true","parameters":[{"name":"fileName","labelKey":"EXPORT_FILENAME","type":"text","defaultValue":{"value":"","labelKey":"EXPORT_FILENAME_DEFAULT"}}]}]}}