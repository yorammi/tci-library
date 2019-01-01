def call(String requirementsFilePath) {
    tciGeneral.deprecatedMessage("tplPythonRequirements","tciPython.installPythonModules")
    tciPython.installPythonModules(requirementsFilePath)
}

