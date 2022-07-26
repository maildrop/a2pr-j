@echo off

@rem ================================
@rem plain text PRinter with Java
@rem ================================
@rem a2pr-j driver for Microsoft Windows cmd.exe 
@rem 
@rem Copyright(C) 2022 , TOGURO Mikito

setlocal 
if not "%1"=="" call :check_file_type "%~1" .xml .c .cpp .h .java
endlocal
GOTO :EOF

@rem �t�@�C���̊g���q�ɉ����āArun_java �̃I�v�V������ύX����B�����ł̓t�@�C���G���R�[�f�B���O
:check_file_type
if "%~x1"=="%2" (call :run_java --utf-8 -f "%~1" ) ELSE (if "%3"=="" (call :run_java -f "%~1") ELSE call :check_file_type "%~1" %3 %4 %5 %6 %7 %8 %9)
exit /b

@rem ���s
:run_java

FOR /F "usebackq" %%I in (`dir /b "%~dp0\a2pr-*-jar-with-dependencies.jar"`) DO java -jar "%%~I" %1 %2 %3
exit /b




