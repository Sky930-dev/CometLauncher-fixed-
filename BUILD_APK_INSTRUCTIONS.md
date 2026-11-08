# Comet Launcher — automatic APK build

This project includes a GitHub Actions workflow that attempts to build an installable **debug APK**. It has not been tested against GitHub's runners yet.

1. Extract `Comet-Launcher-GitHub-Ready.zip` to a folder on your computer.
2. Create a new **private** repository at https://github.com/new (recommended while testing).
3. Open that repository, choose **Add file → Upload files**, and upload the **contents** of the extracted folder (including the hidden `.github` folder). GitHub web upload may not support preserving nested folder structure in all browsers; if so use GitHub Desktop to commit and push the entire extracted project folder.
4. After committing the files to the repository's `main` branch, open **Actions → Build Comet Launcher APK → Run workflow**. A push to `main` should also start a build automatically if workflows are enabled.
5. Open the completed workflow run. Under **Artifacts**, download **Comet-Launcher-APK**. Unzip that artifact to find `app-debug.apk`.
6. Install it on your Android phone. Android may ask you to allow installation from the app you used to open the APK.

If the workflow fails, open the failed step, copy its first meaningful error, and share it for troubleshooting. No successful build has yet been verified.

Notes:
* A debug APK is installable, but not signed for Google Play distribution.
* The project uses Android API 36.1, Gradle 9.3.1, and AGP 9.1.1.
* It may need additional project-level fixes if its Kotlin or Compose code does not compile.
* The original project code was preserved except for switching to standard generated debug signing.
* The project includes Firebase code; online features may require your own Firebase configuration.
