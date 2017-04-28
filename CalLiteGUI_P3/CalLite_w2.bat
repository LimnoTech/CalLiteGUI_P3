del /F /Q C:\Users\tslawecki.LIMNO\git\CalLiteGUI_P3-32\CalLiteGUI_P3\Scenarios\Run_Details\test\run\wsidi_iteration.log
del /F /Q C:\Users\tslawecki.LIMNO\git\CalLiteGUI_P3-32\CalLiteGUI_P3\Scenarios\Run_Details\test\run\progress.txt
del /F /Q C:\Users\tslawecki.LIMNO\git\CalLiteGUI_P3-32\CalLiteGUI_P3\Scenarios\Run_Details\test\run\"=WreslCheck_main=.log"
del /F /Q C:\Users\tslawecki.LIMNO\git\CalLiteGUI_P3-32\CalLiteGUI_P3\Scenarios\Run_Details\test\run\"=WreslCheck_main_wsidi=.log"

timeout /T 3
start /wait /min group_0.bat

exit

