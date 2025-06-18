@echo off
echo ========================================
echo    Compilation de l'APK LiveTV Display
echo ========================================
echo.

echo Verification des prerequis...
if not exist "display_client_android\gradlew.bat" (
    echo ERREUR: Le projet Android n'est pas trouve dans display_client_android
    pause
    exit /b 1
)

echo Navigation vers le dossier du projet...
cd display_client_android

echo.
echo Nettoyage du projet...
call gradlew.bat clean

echo.
echo Compilation de l'APK de debug...
call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo    COMPILATION REUSSIE !
    echo ========================================
    echo.
    echo L'APK a ete genere dans :
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Vous pouvez maintenant installer cet APK sur votre appareil Android/TV.
    echo.
    
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        echo Taille du fichier APK :
        dir "app\build\outputs\apk\debug\app-debug.apk" | find "app-debug.apk"
        echo.
        echo Pour installer sur un appareil connecte via ADB :
        echo adb install app\build\outputs\apk\debug\app-debug.apk
    )
) else (
    echo.
    echo ========================================
    echo    ERREUR DE COMPILATION !
    echo ========================================
    echo.
    echo Verifiez les erreurs ci-dessus et corrigez-les avant de reessayer.
    echo.
    echo Erreurs communes :
    echo - Java JDK 11 ou superieur requis
    echo - Android SDK manquant
    echo - Erreurs de syntaxe dans le code
)

echo.
pause
