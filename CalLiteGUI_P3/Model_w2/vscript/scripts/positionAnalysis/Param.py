import os

logger = None
logFile = '=PA=.log'
#runTemplateFile = 'runBatch.template'
runTemplateFile = { 'wrims1': 'runBatch.template.wrims1', 'wrims2': 'runBatch.template.wrims2'}
#templateDir = os.path.join(os.path.split(__file__)[0], r'..\templates')
templateDir = os.path.dirname(os.path.split(__file__)[0])
templateDir = os.path.normpath(templateDir)
runBatchPrepend = 'PA_'