
const removeNoDimensionImages = require('/usr/local/lib/svgo/removeNoDimensionImages.js');
module.exports = {
    multipass: true,

    plugins: [
        'preset-default',
        removeNoDimensionImages,
    ],
};
