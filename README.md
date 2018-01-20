Horario
==========
[![Build Status](https://travis-ci.org/AChep/horario.svg?branch=master)](https://travis-ci.org/AChep/horario) [![Crowdin](https://d322cqt584bo4o.cloudfront.net/horario/localized.svg)](https://crowdin.com/project/horario)

<img alt="Logo" align="right" height="220"
   src="https://github.com/AChep/horario/raw/master/extras/ic_launcher_web.png" />

*Are you a student?*

Horario app is a beautiful Material Design app, which helps you to manage your classes and exams in the most convenient way. Fill your timetable once and it will be with you wherever you go, because of sync across all your Android devices. Notifications won’t let you forget about your homework and exams.

 - **[Help us to translate it](https://crowdin.com/project/horario)** _(even a short look would be helpful)_
 - **[Contibution rules](https://github.com/AChep/horario/blob/master/CONTRIBUTING.md)** _(in cause you are a software engineer)_

<a href="https://blockr.io/address/info/1GYj49ZnMByKj2f6p7r4f92GQi5pR6BSMz">
  <img alt="Bitcoin wallet: 1GYj49ZnMByKj2f6p7r4f92GQi5pR6BSMz" vspace="28" hspace="20"
       src="https://github.com/AChep/horario/raw/master/extras/btn_bitcoin.png" />
</a> <a href="http://goo.gl/UrecGo">
  <img alt="PayPal" vspace="28"
       src="https://github.com/AChep/horario/raw/master/extras/btn_paypal.png" />
</a>

Report a bug or request a feature
----------------
Before creating a new issue please make sure that same or similar issue is not already created by checking [open issues][2] and [closed issues][3] *(please note that there might be multiple pages)*. If your issue is already there, don't create a new one, but leave a comment under already existing one.

Checklist for creating issues:

- Keep titles short but descriptive.
- For feature requests leave a clear description about the feature with examples where appropriate.
- For bug reports leave as much information as possible about your device, android version, etc.
- For bug reports also write steps to reproduce the issue.

[Create new issue][1]

Creating your Horario
----------------
We welcome all developers to use our source code to create applications on our platform.
There are several things we require from **all developers** for the moment:

1. Please **do not** use the name Horario for your app — or make sure your users understand that it is unofficial.
2. Kindly **do not** use our standard logo as your app's logo.
3. Please remember to read and follow the [license][4].

Versioning
----------------
For transparency in a release cycle and in striving to maintain backward compatibility, a project should be maintained under the Semantic Versioning guidelines. Sometimes we screw up, but we should adhere to these rules whenever possible.

Releases will be numbered with the following format: `<major>.<minor>.<patch>` and constructed with the following guidelines:
- Breaking backward compatibility bumps the major while resetting minor and patch
- New additions without breaking backward compatibility bumps the minor while resetting the patch
- Bug fixes and misc changes bumps only the patch

For more information on SemVer, please visit http://semver.org/.

Build
----------------
Clone the project and come in:

``` bash
$ git clone git://github.com/AChep/horario.git
$ cd horario/
```

Repository doesn't include `app/release.properties`, `app/release-key.keystore` and `app/google-services.json` so you have to create them manually before building project.

##### app/release-key.keystore
Check out this answer ["How can I create a keystore?"](http://stackoverflow.com/a/15330139/1408535)
##### app/release.properties
The structure of the file:
```
key_alias=****
key_google_play_public=****
password_store=****
password_key=****
```
| Key | Description |
| --- | --- |
| `key_alias` | Key alias used to generate keystore  |
| `key_google_play_public` | Google Play license key (may be random string.)   |
| `password_store` | Your keystore password |
| `password_key` | Your alias key password |
##### app/google-services.json
Generate one in [Google Developers console](https://console.developers.google.com)


[1]: https://github.com/AChep/horario/issues/new
[2]: https://github.com/AChep/horario/issues?state=open
[3]: https://github.com/AChep/horario/issues?state=closed
[4]: https://github.com/AChep/horario/blob/master/LICENSE
