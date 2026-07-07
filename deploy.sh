git pull

cd /Users/choihyun-young/IdeaProjects/resume
JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew bootJar
cp build/libs/resume-0.0.1-SNAPSHOT.jar ~/apps/resume/resume.jar
launchctl kickstart -k gui/$(id -u)/com.hypepia.resume

tail -f ~/Library/Logs/resume-app.out.log