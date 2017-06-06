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

function getQuery(event) {
  let filter = [
    { exists: { field: "location" } },
    { exists: { field: "area" } },
    { exists: { field: "eurPrice" } },
    { exists: { field: "topreality.link" } },
    { exists: { field: "topreality.image" } }
  ];

  if (event.queryStringParameters !== undefined && event.queryStringParameters !== null) {
    const priceMin = event.queryStringParameters.priceMin;
    const priceMax = event.queryStringParameters.priceMax;
    const areaMin = event.queryStringParameters.areaMin;
    const areaMax = event.queryStringParameters.areaMax;
    const latTop = event.queryStringParameters.latTop;
    const latBottom = event.queryStringParameters.latBottom;
    const lonWest = event.queryStringParameters.lonWest;
    const lonEast = event.queryStringParameters.lonEast;

    if (priceMin !== undefined || priceMin !== undefined) {
      let eurPrice = { gte: priceMin, lte: priceMax };
      filter.push({ range: { "eurPrice": eurPrice} });
    }

    if (areaMin !== undefined || areaMax !== undefined) {
      let area = { gte: areaMin, lte: areaMax };
      filter.push({ range: { "area": area} });
    }

    if (latTop !== undefined && latBottom !== undefined && lonWest !== undefined && lonEast !== undefined) {
      let location = {
        top_left: { lat: latTop, lon: lonWest },
        bottom_right: { lat: latBottom, lon: lonEast }
      }
      filter.push({ geo_bounding_box: { "location": location } });
    }
  }

  return {
    index: 'rents',
    size: 200,
    body: {
      query: {
        bool: {
          filter: filter
        }
      }
    }
  }
}

function mapElasticResult(item) {
  const source = item._source;
  return {
    area: source.area,
    price: source.eurPrice,
    title: source['topreality.title'],
    link: source['topreality.link'],
    image: source['topreality.image'],
    location: source.location
  }
}

module.exports.getapartments = (event, context, callback) => {
  es.search(getQuery(event), function(error, result) {
    if (error) {
      console.log('Error:' + error);
      const response = {
        statusCode: 500,
        headers: { 'Access-Control-Allow-Origin': '*' },
        body: "Internal server error."
      };
      callback(null, response);  
    } else {
      const results = result.hits.hits.map(mapElasticResult);
      const response = {
        statusCode: 200,
        headers: { 'Access-Control-Allow-Origin': '*' },
        // TODO notify if result.hits.total too big
        body: JSON.stringify(results)
      };
      callback(null, response);
    }
  });
};
