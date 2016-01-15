# Send multiple data through Bluetooth between Android and Arduino

Purpose of this app is send data (strings, being more specific) to Arduino through a bluetooth connection. Exciting thing is that, it won't be sent just a single byte but a string, it means several bytes at time.

The entire project is based on the control of a RGB LED. Indeed, using an adroid application built ad-hoc for this project, we can turn on/off every single led that composes RGB LED (so red led, green led and blue led) and also the intensity of each one.

It works in the following way:
* User opens the application
* App automatically connects to arduino bluetooth module
* After user pressed a button inside the app (ex. the red one), Android sends a string (ex. "RED ON:") to the serial communication with bluetooth module (Note that ':' is an escape char I chose that says to arduino that command string finished)
* Arduino fetch the command from the serial communication and execute it

## The circuit

### Components
* 1 Arduino (UNO rev 3 in pic)
* 1 Bluetooth module* (HC-06 in pic)
* 1 RGB LED
* 3 220Ω resistences
* Wires


*Note that you have to know the MAC address and insert it into MainActivity.java code. If you don't know how to find it, after created the circuit and turned it on you can find it using an app such as https://play.google.com/store/apps/details?id=com.ccpcreations.android.bluetoothmacfinder&hl=it
![The circuit](http://s30.postimg.org/g34qtppvl/20160115_111830.jpg)


## The Application

The application is a very light and has a simple interface. Source is in the "Android Project" folder.

![The Application](http://s2.postimg.org/x8vv9tdpl/Screenshot_2016_01_15_12_58_23.png)

