@echo off

for /f "tokens=1-3*" %%a in ("%*") do (
    set par1=%%a
    set par2=%%b
	set par3=%%c
)

echo the script is %0
echo Installable unit is %par1%
echo Source repository is %par2%
echo Destination is %par3%

java -jar D:\back-end-p2\P2-Server\build\p2Container\p2Agent\plugins\org.eclipse.equinox.launcher-1.5.0.jar -application org.eclipse.equinox.p2.director -installIU %par1% -repository %par2% -destination %par3%