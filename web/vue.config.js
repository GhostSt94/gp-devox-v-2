const port = require('./../config.json').clientPort;
const webpack = require('webpack');

module.exports = {
	configureWebpack: {
		plugins: [
			new webpack.ProvidePlugin({
				$: 'jquery',
				jquery: 'jquery',
				'window.jQuery': 'jquery',
				jQuery: 'jquery',
				_: 'lodash'
			})
		]
	},
	devServer: {port},
};
