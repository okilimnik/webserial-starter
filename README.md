WebSerial 
=========
#### A browser based Serial Port application: https://webserial-8b7dd.web.app/
#### Ported from https://github.com/williamkapke/webserial into Clojurescript and Replicant

<img src="screenshot-1.png" width="640">
<img src="screenshot-2.png" width="640">

### Huawei Freebuds commands (I have only this device to test it on):
The header and the checksum are added for you, you need to type only a command and the parameters (with their length).
Noise cancelling on:
```script
+␄␁␁␁
```
Noise cancelling off:
```script
+␄␁␁␀
```
Look for more commands in the original [article](https://mmk.pw/en/posts/freebuds-4i-proto/)

### Tests
Running tests require headless Chrome installed.
```bash
npm run test
```