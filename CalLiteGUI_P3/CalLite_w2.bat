@title=CalLiteRunlong3
del /F /Q C:\Users\tslawecki.LIMNO\git\CalLiteGUI_P3\CalLiteGUI_P3\Scenarios\Run_Details\long3\run\progress.txt
del /F /Q C:\Users\tslawecki.LIMNO\git\CalLiteGUI_P3\CalLiteGUI_P3\Scenarios\Run_Details\long3\run\"=WreslCheck_main=.log"
del /F /Q C:\Users\tslawecki.LIMNO\git\CalLiteGUI_P3\CalLiteGUI_P3\Scenarios\Run_Details\long3\run\"=WreslCheck_main_wsidi=.log"
del /F /Q C:\Users\tslawecki.LIMNO\git\CalLiteGUI_P3\CalLiteGUI_P3\Scenarios\Run_Details\long3\run\wsidi_iteration.log


Model_w2\vscript.bat Model_w2\vscript\Main.py "C:\Users\tslawecki.LIMNO\git\CalLiteGUI_P3\CalLiteGUI_P3\Scenarios\Run_Details\long3\long3_wsidi.config" 3
