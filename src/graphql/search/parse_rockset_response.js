exports.handler = async (event, context, callback) => {

    console.log("context: ", context);
    console.log("VTL details: ", event);

    event[0].results.forEach(x => {
        console.log(JSON.stringify(x));
    });
    return event[0].results;
};