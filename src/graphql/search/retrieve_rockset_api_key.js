
exports.handler = async (event, context, callback) => {

    const AWS = require('aws-sdk')

    AWS.config.update({
        region: process.env.region
    })

    const parameterStore = new AWS.SSM()

    const getParam = param => {
        return new Promise((res, rej) => {
            parameterStore.getParameter({
                Name: param
            }, (err, data) => {
                if (err) {
                    return rej(err)
                }
                return res(data)
            })
        })
    }
    const stage = process.env.stage;
    const param = await getParam(stage+'-rockset-api-key');

    console.log('ssm = ' + JSON.stringify(param));
    console.log('context: ', context);
    console.log('VTL details: ', event);
    callback(null, param);
};