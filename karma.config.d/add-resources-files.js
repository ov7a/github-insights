let nodeModulesIndex = config.basePath.lastIndexOf("/node_modules");
let projectName = config.basePath.substring(
    config.basePath.lastIndexOf("/", nodeModulesIndex - 1) + 1,
    nodeModulesIndex
);

config.basePath = config.basePath + "/../kotlin";
config.files = config.files.concat([{
    "pattern": `**/!(${projectName}.d.ts|${projectName}.js|${projectName}.js.map)`,
    "included": false
}]);
