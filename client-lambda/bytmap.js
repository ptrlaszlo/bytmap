'use strict';

const accessKey = process.env.BYTMAP_AWS_ACCESS_KEY;
const secretKey = process.env.BYTMAP_AWS_SECRET_KEY;
const region = process.env.BYTMAP_AWS_REGION;
const elasticHost = process.env.BYTMAP_ES_URL;

const es = require('elasticsearch').Client({
  hosts: elasticHost,
  connectionClass: require('http-aws-es'),
  amazonES: {
    region: region,
    accessKey: accessKey,
    secretKey: secretKey
  }
});

module.exports.getapartments = (event, context, callback) => {
  es.search({
    index: 'rents',
    size: 500,
    body: {
      query: {
        exists: {
          field: "location"
        }
      }
    }
  }, function(error, result) {
    if (error) {
      console.log('Error:' + error);
      const response = {
        statusCode: 500,
        headers: { 'Access-Control-Allow-Origin': '*' },
        body: "Internal server error."
      };
      callback(null, response);  
    } else {
      const response = {
        statusCode: 200,
        headers: { 'Access-Control-Allow-Origin': '*' },
        body: JSON.stringify(result.hits.hits)
      };
      callback(null, response);
    }
  });
};
