function isNumeric(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

function readApartments(bounds: google.maps.LatLngBounds) {
  if (bounds) {
    let numericParams = '';
    const areaMin = (<HTMLInputElement>document.getElementById('areaMin')).value;
    if (isNumeric(areaMin)) { numericParams += `&areaMin=${areaMin}` }
    const areaMax = (<HTMLInputElement>document.getElementById('areaMax')).value;
    if (isNumeric(areaMax)) { numericParams += `&areaMax=${areaMax}` }
    const priceMin = (<HTMLInputElement>document.getElementById('priceMin')).value;
    if (isNumeric(priceMin)) { numericParams += `&priceMin=${priceMin}` }
    const priceMax = (<HTMLInputElement>document.getElementById('priceMax')).value;
    if (isNumeric(priceMax)) { numericParams += `&priceMax=${priceMax}` }

    const nelat = bounds.getNorthEast().lat();
    const nelon = bounds.getNorthEast().lng();
    const swlat = bounds.getSouthWest().lat();
    const swlon = bounds.getSouthWest().lng();
    const opts = { method: 'GET', headers: {} };
    const params = `latTop=${nelat}&latBottom=${swlat}&lonWest=${swlon}&lonEast=${nelon}` + numericParams;
    fetch(`https://fozix1n9gl.execute-api.eu-west-1.amazonaws.com/dev/apartments?${params}`, opts).then(function (response) {
      return response.json();
    }).then(function (body: Array<Rent>){
      plotMarkers(body);
    });
  }
}
