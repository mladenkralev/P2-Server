@echo off

for /f "tokens=1-3*" %%a in ("%*") do (
    set par1=%%a
    set par2=%%b
)

echo the script is %0
echo Source of bundles %par1%
echo Artifactory and Metadata source is %par2%

java -jar D:\back-end-p2\P2-Server\build\p2Container\p2Agent\plugins\org.eclipse.equinox.launcher-1.5.0.jar -application org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher -artifactRepository %par2% -metadataRepository %par2% -source %par1% -publishArtifacts