/*
* Main file for the KIIIB project
* Authors: Andreas Møller, David Emil Lemvigh
*/

package ai.impl
import core._
import core.devices._
import core.messages._
import java.util.Timer
import utils.RichTimer._
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
class Ai extends AiListener {
    def aiStarted(ctrl: AiController): Unit = { }
    def aiStopped: Unit = { }
    var conn:Connection = null
    try {
        Class.forName("com.mysql.jdbc.Driver")//load the mysql driver
        conn = DriverManager.getConnection("jdbc:mysql://localhost/kiiib?user=KIIIB&password=42")//connect to the database
     }
     catch {
        case sqle:SQLException =>
        println("SQLException: " + sqle.getMessage())
        println("SQLState: " + sqle.getSQLState())
        println("VendorError: " + sqle.getErrorCode())
        case e:Exception =>
           e.printStackTrace()
       }

    /*
    *deviceEventRecieved handles events monitored by the AI
    */
    def deviceEventReceived(c: AiController,id: MasterDeviceId,device: Device,event: DeviceEvent): Unit = {
        (device, event) match {
            case (_: BinarySwitch, TurnedOn(time)) => //switch turned on 
                var stmt = conn.createStatement()
                stmt.executeUpdate("INSERT INTO switch_events VALUES("+id+",1,NOW())")
                //update "markov" table here?
            println("switched on "+id)
            c.connectionsFrom(id) foreach {
                x => c.sendDeviceCommand(x.to, TurnOn)
            }
            case (_: BinarySwitch, TurnedOff(time)) => // switch turned off
                var stmt = conn.createStatement()
                stmt.executeUpdate("INSERT INTO switch_events VALUES("+id+",0,NOW())")
                // update markov table here?
                println("switched off "+id)
                c.connectionsFrom(id) foreach {
                    x => c.sendDeviceCommand(x.to, TurnOff)
                }
            case (_: MotionSensor, MotionEvent(time)) => // motion sensor events
                var stmt = conn.createStatement()
                stmt.executeUpdate("INSERT INTO sensor_events VALUES("+id+",NOW())")
                println("Sensor id :"+id)
            case _ => ()
        }
    }
}

// vim: set ts=2 sw=2 et:


// vim: set ts=4 sw=4 et:
