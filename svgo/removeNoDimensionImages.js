'use strict';

exports.name = 'removeNodDimensionImages';
exports.type = 'visitor';
exports.active = true;
exports.description = 'removes images without width or height attributes (disabled by default)';

/**
 * Remove raster images references in <image>.
 *
 * @see https://bugs.webkit.org/show_bug.cgi?id=63548
 *
 * @author Kir Belevich
 *
 * @type {import('../lib/types').Plugin<void>}
 */
exports.fn = () => {
  return {
    element: {
        enter: (node, parentNode) => {
            if (node.name === 'image' && !(node.attributes.width  && node.attributes.height)) {
                parentNode.children = parentNode.children.filter((child) => child !== node);
            }
        },
    },
  };
};
