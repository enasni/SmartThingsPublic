/* **DISCLAIMER**
* THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
* Without limitation of the foregoing, Contributors/Regents expressly does not warrant that:
* 1. the software will meet your requirements or expectations;
* 2. the software or the software content will be free of bugs, errors, viruses or other defects;
* 3. any results, output, or data provided through or generated by the software will be accurate, up-to-date, complete or reliable;
* 4. the software will be compatible with third party software;
* 5. any errors in the software will be corrected.
* The user assumes all responsibility for selecting the software and for the results obtained from the use of the software. The user shall bear the entire risk as to the quality and the performance of the software.
*/ 

def clientVersion() {
    return "01.06.02"
}

/*
* Garage Door Open and Close
*
* Copyright RBoy Apps
* Redistribution of any changes or code is not allowed without permission
*
* Change Log
* 2017-5-26 - (v 01.06.02) Multiple SMS numbers are now separate by a *
* 2017-4-29 - (v01.06.01) Patch for delayed opening of garage doors
* 2017-4-22 - (v01.06.00) Added support for delayed opening of garage doors
* 2016-11-5 - Added support for automatic code update notifications and fixed an issue with sms
* 2016-10-7 - Added support for Operating Schedule for arrival and departure
* 2016-8-17 - Added workaround for ST contact address book bug
* 2016-8-13 - Added support for sending SMS to multiple numbers by separating them with a +
* 2016-8-13 - Added support for contact address book from ST
* 2016-8-13 - Added support to turn on lights when someone arrives with option of doing it when it's dark outside
* 2016-2-14 - Only open/close doors if required and notify accordingly
* 2016-1-16 - Description correction
* 2016-1-16 - Added option to choose different garage doors/people for Open and Close actions
* 2016-1-15 - Added option for notitifications
* 2016-1-15 - Fix for missing handler
* 2015-10-26 - Fixed incorrect display text for arriving
* Updated 2015-2-2 - Initial release
*
*/
definition(
    name: "Garage Door Open and Close Automatically when People Arrive/Leave",
    namespace: "rboy",
    author: "RBoy Apps",
    description: "Open a garage door when someone arrives, Close a garage door when someone leaves",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact@2x.png")

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Garage Door Open and Close Automatically when People Arrive/Leave v${clientVersion()}", install: true, uninstall: true) {    
        section("Open Garage Doors When People Arrive", hidden: false, hideable: true) {
            input "arrives", "capability.presenceSensor", title: "When one of these arrive", description: "Which people arrive?", multiple: true, required: false
            input "doorsOpen", "capability.doorControl", title: "Open these garage door(s)?", required: false, multiple: true
            input "doorsOpenDelay", "number", title: "...after these seconds", required: false
            input "arriveSwitches", "capability.switch", title: "...and turn on these switches", description: "Turn on lights", multiple: true, required: false, submitOnChange: true
            if (arriveSwitches) {
                input "arriveAfterDark", "bool", title: "...only if it's getting dark outside", description: "Turn on lights at night", required: false
            }
        }
        for (i in 0..0) { // 1 Schedules allowed
            def usr = "A"
            def priorUserDayOfWeek = settings."userDayOfWeek${usr}${i}"
            def priorUserStartTime = settings."userStartTime${usr}${i}"
            def priorUserEndTime = settings."userEndTime${usr}${i}"

            section("Arrival Operating Schedule (optional)", hidden: (priorUserDayOfWeek || priorUserStartTime || priorUserEndTime ? false : true), hideable: true) {
                input "userStartTime${usr}${i}", "time", title: "Start Time", required: false
                input "userEndTime${usr}${i}", "time", title: "End Time", required: false
                input name: "userDayOfWeek${usr}${i}",
                    type: "enum",
                    title: "Which day of the week?",
                    required: false,
                    multiple: true,
                    options: [
                        'All Week',
                        'Monday to Friday',
                        'Saturday & Sunday',
                        'Monday',
                        'Tuesday',
                        'Wednesday',
                        'Thursday',
                        'Friday',
                        'Saturday',
                        'Sunday'
                    ],
                    defaultValue: priorUserDayOfWeek
            }
        }
        section("Close Garage Doors When People Leave", hidden: false, hideable: true) {
            input "leaves", "capability.presenceSensor", title: "When one of these leave", description: "Which people leave?", multiple: true, required: false
            input "doorsClose", "capability.doorControl", title: "Close these garage door(s)?", required: false, multiple: true
        }
        for (i in 0..0) { // 1 Schedules allowed
            def usr = "B"
            def priorUserDayOfWeek = settings."userDayOfWeek${usr}${i}"
            def priorUserStartTime = settings."userStartTime${usr}${i}"
            def priorUserEndTime = settings."userEndTime${usr}${i}"

            section("Departure Operating Schedule (optional)", hidden: (priorUserDayOfWeek || priorUserStartTime || priorUserEndTime ? false : true), hideable: true) {
                input "userStartTime${usr}${i}", "time", title: "Start Time", required: false
                input "userEndTime${usr}${i}", "time", title: "End Time", required: false
                input name: "userDayOfWeek${usr}${i}",
                    type: "enum",
                    title: "Which day of the week?",
                    required: false,
                    multiple: true,
                    options: [
                        'All Week',
                        'Monday to Friday',
                        'Saturday & Sunday',
                        'Monday',
                        'Tuesday',
                        'Wednesday',
                        'Thursday',
                        'Friday',
                        'Saturday',
                        'Sunday'
                    ],
                    defaultValue: priorUserDayOfWeek
            }
        }
        section("Notifications") {
            input("recipients", "contact", title: "Send notifications to (optional)", multiple: true, required: false) {
                paragraph "You can enter multiple phone numbers to send an SMS to by separating them with a '*'. E.g. 5551234567*4447654321"
                input "sms", "phone", title: "Send SMS to (phone number)", required: false
                input "push", "bool", title: "Send push notification", defaultValue: "true"
            }
        }
        section() {
            label title: "Assign a name for this SmartApp (optional)", required: false
            input name: "disableUpdateNotifications", title: "Don't check for new versions of the app", type: "bool", required: false
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unschedule()
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(arrives, "presence.present", arriveHandler)
    subscribe(leaves, "presence.not present", leaveHandler)
        
    // Check for new versions of the code
    def random = new Random()
    Integer randomHour = random.nextInt(18-10) + 10
    Integer randomDayOfWeek = random.nextInt(7-1) + 1 // 1 to 7
    schedule("0 0 " + randomHour + " ? * " + randomDayOfWeek, checkForCodeUpdate) // Check for code updates once a week at a random day and time between 10am and 6pm
}

def delayOpenDoors(data) {
    log.debug "Delayed arriveHandler presenceId: $data.deviceNetworkId, type: $data.type"
    
    def device = settings.arrives.find { it.deviceNetworkId == data.deviceNetworkId }
    log.trace "Presence sensor: $device.displayName"
    
    def msg = "$delayOpenDoors seconds elapsed"
    
    if (device.currentPresence != "present") {
        msg += ", $device.displayName not present, skipping opening doors"
    } else {
        for(door in doorsOpen) {
            if (door.currentDoor == "closed") {
                msg += ", opening $door"
                door.open()
            } else {
                msg += ", $door already open"
            }
        }
    }

    log.debug(msg)
    sendNotifications(msg)
}

def arriveHandler(evt)
{
    log.debug "arriveHandler $evt.displayName, $evt.name: $evt.value"

    if (!checkSchedule(0, "A")) { // Check if we are within operating Schedule to operating things
        log.warn "Out of operating schedules, skipping arrival handling"
        return
    }

    def msg = "$evt.displayName arrived"
    
    if (doorsOpenDelay) {
        msg += ", opening doors after $doorsOpenDelay seconds"
        runIn(doorsOpenDelay, "delayOpenDoors", [data: [deviceNetworkId: evt.device.deviceNetworkId, type: evt.value]])        
    } else {
        for(door in doorsOpen) {
            if (door.currentDoor == "closed") {
                msg += ", opening $door"
                door.open()
            } else {
                msg += ", $door already open"
            }
        }
    }

    if (arriveAfterDark) {
        def cdt = new Date(now())
        def sunsetSunrise = getSunriseAndSunset(sunsetOffset: "-01:00") // Turn on 1 hour before sunset (dark)
        log.trace "Current DT: $cdt, Sunset $sunsetSunrise.sunset, Sunrise $sunsetSunrise.sunrise"
        if ((cdt >= sunsetSunrise.sunset) || (cdt <= sunsetSunrise.sunrise)) {
            arriveSwitches?.on() // Turn on switches after dark
            msg += ", turning on $arriveSwitches because it's getting dark outside"
        }
    } else {
        arriveSwitches?.on() // Turn on switches on arrival
        msg += ", turning on $arriveSwitches"
    }

    log.debug(msg)
    sendNotifications(msg)
}

def leaveHandler(evt)
{
    log.debug "leaveHandler $evt.displayName, $evt.name: $evt.value"

    if (!checkSchedule(0, "B")) { // Check if we are within operating Schedule to operating things
        log.warn "Out of operating schedules, skipping departure handling"
        return
    }

    def msg = "$evt.displayName left"
    for(door in doorsClose) {
        if (door.currentDoor == "open") {
            msg += ", closing $door"
            door.close()
        } else {
            msg += ", $door already closed"
        }
    }
    
    log.debug(msg)
    sendNotifications(msg)
}

private void sendText(number, message) {
    if (number) {
        def phones = number.split("\\*")
        for (phone in phones) {
            sendSms(phone, message)
        }
    }
}

private void sendNotifications(message) {
    if (location.contactBookEnabled) {
        sendNotificationToContacts(message, recipients)
    } else {
        if (push) {
            sendPush message
        } else {
            sendNotificationEvent(message)
        }
        if (sms) {
            sendText(sms, message)
        }
    }
}

// Checks if we are within the current operating scheduled
// Inputs to the function are user (i) and schedule (x) (there can be multiple schedules)
// Preferences required in user input settings are:
// settings."userStartTime${x}${i}"
// settings."userEndTime${x}${i}"
// settings."userDayOfWeek${x}${i}"
private checkSchedule(def i, def x) {
    log.trace("Checking operating schedule $x for user $i")

    TimeZone timeZone = location.timeZone
    if (!timeZone) {
        timeZone = TimeZone.getDefault()
        log.error "Hub timeZone not set, using ${timeZone.getDisplayName()} timezone. Please set Hub location and timezone for the codes to work accurately"
        sendPush "Hub timeZone not set, using ${timeZone.getDisplayName()} timezone. Please set Hub location and timezone for the codes to work accurately"
    }

    def doChange = false
    Calendar localCalendar = Calendar.getInstance(timeZone);
    int currentDayOfWeek = localCalendar.get(Calendar.DAY_OF_WEEK);
    def currentDT = new Date(now())

    // some debugging in order to make sure things are working correclty
    log.trace "Current time: ${currentDT.format("EEE MMM dd yyyy HH:mm z", timeZone)}"

    // Check if we are within operating times
    if (settings."userStartTime${x}${i}" != null && settings."userEndTime${x}${i}" != null) {
        def scheduledStart = timeToday(settings."userStartTime${x}${i}", timeZone)
        def scheduledEnd = timeToday(settings."userEndTime${x}${i}", timeZone)

        if (scheduledEnd <= scheduledStart) { // End time is next day
            log.trace "End time is before start time, assuming it is the next day"
            scheduledEnd = scheduledEnd.next() // Get the time for tomorrow
        }

        log.trace("Operating Start ${scheduledStart.format("EEE MMM dd yyyy HH:mm z", timeZone)}, End ${scheduledEnd.format("EEE MMM dd yyyy HH:mm z", timeZone)}")

        if (currentDT < scheduledStart || currentDT > scheduledEnd) {
            log.info("Outside operating time schedule")
            return false
        }
    }

    // Check the condition under which we want this to run now
    // This set allows the most flexibility.
    log.trace("Operating DOW(s): ${settings."userDayOfWeek${x}${i}"}")

    if(settings."userDayOfWeek${x}${i}" == null) {
        log.warn "Day of week not specified for operating schedule $x for user $i, assuming no schedule set, so we are within schedule"
        return true
    } else if(settings."userDayOfWeek${x}${i}".contains('All Week')) {
        doChange = true
    } else if((settings."userDayOfWeek${x}${i}".contains('Monday') || settings."userDayOfWeek${x}${i}".contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.MONDAY) {
        doChange = true
    } else if((settings."userDayOfWeek${x}${i}".contains('Tuesday') || settings."userDayOfWeek${x}${i}".contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.TUESDAY) {
        doChange = true
    } else if((settings."userDayOfWeek${x}${i}".contains('Wednesday') || settings."userDayOfWeek${x}${i}".contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.WEDNESDAY) {
        doChange = true
    } else if((settings."userDayOfWeek${x}${i}".contains('Thursday') || settings."userDayOfWeek${x}${i}".contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.THURSDAY) {
        doChange = true
    } else if((settings."userDayOfWeek${x}${i}".contains('Friday') || settings."userDayOfWeek${x}${i}".contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.FRIDAY) {
        doChange = true
    } else if((settings."userDayOfWeek${x}${i}".contains('Saturday') || settings."userDayOfWeek${x}${i}".contains('Saturday & Sunday')) && currentDayOfWeek == Calendar.instance.SATURDAY) {
        doChange = true
    } else if((settings."userDayOfWeek${x}${i}".contains('Sunday') || settings."userDayOfWeek${x}${i}".contains('Saturday & Sunday')) && currentDayOfWeek == Calendar.instance.SUNDAY) {
        doChange = true
    }


    // If we have hit the condition to schedule this then lets do it
    if(doChange == true){
        log.info("Within operating schedule")
        return true
    }
    else {
        log.info("Outside operating schedule")
        return false
    }
}

def checkForCodeUpdate(evt) {
    log.trace "Getting latest version data from the RBoy Apps server"
    
    def appName = "Garage Door Open and Close Automatically when People Arrive/Leave"
    def serverUrl = "http://smartthings.rboyapps.com"
    def serverPath = "/CodeVersions.json"
    
    try {
        httpGet([
            uri: serverUrl,
            path: serverPath
        ]) { ret ->
            log.trace "Received response from RBoy Apps Server, headers=${ret.headers.'Content-Type'}, status=$ret.status"
            //ret.headers.each {
            //    log.trace "${it.name} : ${it.value}"
            //}

            if (ret.data) {
                log.trace "Response>" + ret.data
                
                // Check for app version updates
                def appVersion = ret.data?."$appName"
                if (appVersion > clientVersion()) {
                    def msg = "New version of app ${app.label} available: $appVersion, current version: ${clientVersion()}.\nPlease visit $serverUrl to get the latest version."
                    log.info msg
                    if (!disableUpdateNotifications) {
                        sendPush(msg)
                    }
                } else {
                    log.trace "No new app version found, latest version: $appVersion"
                }
                
                // Check device handler version updates
                def caps = [ arrives, doorsOpen, arriveSwitches, leaves, doorsClose ]
                caps?.each {
                    def devices = it?.findAll { it.hasAttribute("codeVersion") }
                    for (device in devices) {
                        if (device) {
                            def deviceName = device?.currentValue("dhName")
                            def deviceVersion = ret.data?."$deviceName"
                            if (deviceVersion && (deviceVersion > device?.currentValue("codeVersion"))) {
                                def msg = "New version of device ${device?.displayName} available: $deviceVersion, current version: ${device?.currentValue("codeVersion")}.\nPlease visit $serverUrl to get the latest version."
                                log.info msg
                                if (!disableUpdateNotifications) {
                                    sendPush(msg)
                                }
                            } else {
                                log.trace "No new device version found for $deviceName, latest version: $deviceVersion, current version: ${device?.currentValue("codeVersion")}"
                            }
                        }
                    }
                }
            } else {
                log.error "No response to query"
            }
        }
    } catch (e) {
        log.error "Exception while querying latest app version: $e"
    }
}