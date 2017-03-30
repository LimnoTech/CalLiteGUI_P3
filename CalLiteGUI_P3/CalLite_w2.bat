@title=CalLiteRunTest
del /F /Q P:\CalGUI_20161109\Scenarios\Run_Details\Test\run\progress.txt
del /F /Q P:\CalGUI_20161109\Scenarios\Run_Details\Test\run\"=WreslCheck_main=.log"
del /F /Q P:\CalGUI_20161109\Scenarios\Run_Details\Test\run\"=WreslCheck_main_wsidi=.log"
del /F /Q P:\CalGUI_20161109\Scenarios\Run_Details\Test\run\wsidi_iteration.log


Model_w2\vscript.bat Model_w2\vscript\Main.py "P:\CalGUI_20161109\Scenarios\Run_Details\Test\Test_wsidi.config" 3
