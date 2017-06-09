function generateContent(item: Rent) {
  let title;
  if (item.title.length > 50) {
    title = item.title.substr(0, 47) + '...';
  } else {
    title = item.title;
  }
  return `
    <a target="_blank" href="${item.link}" class="infobox">
    <img src="${item.image}">
    <span class="title">${title}</span><br>
    <span class="price">Cena: €${parseInt(item.price)}/mesiac</span>
    <span>Plocha: ${item.area}m<sup>2</sup></span>
    </a>
  `;
}
