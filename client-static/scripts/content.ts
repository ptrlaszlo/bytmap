function generateContent(item: Rent) {
  return `
    <a target="_blank" href="${item.link}" style="text-decoration:none;">
    <img src="${item.image}" style="float: left;padding-right:5px;"><br>
    ${item.title}<br><br>
    €${parseInt(item.price)}, ${item.area}m<sup>2</sup>
    </a>
  `;
}
