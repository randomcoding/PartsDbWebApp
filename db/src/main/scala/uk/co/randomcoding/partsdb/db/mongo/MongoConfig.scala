/**
 *
 */
package uk.co.randomcoding.partsdb.db.mongo

/**
 * Provides access to MongoDB collection instances.
 *
 * @author RandomCoder <randomcoder@randomcoding.co.uk>
 *
 */

import com.mongodb.Mongo

import net.liftweb._
import json._
import mongodb._
import common._

/**
 * Initialisation for Mongo DB
 *
 * @author RandomCoder
 *
 * Created On: 19 Aug 2011
 *
 */
object MongoConfig extends Logger {

  private implicit val formats = DefaultFormats

  private var usingCloudfoundry = false
  private var cloudfoundryDbName = ""

  // Case classes required to parse CloudFoundry JSON                                                                                                      
  case class CloudFoundryMongo(name: String, label: String, plan: String, credentials: CloudFoundryMongoCredentials)
  case class CloudFoundryMongoCredentials(hostname: String, port: String, username: String, password: String, name: String, db: String)

  /**
   * Initialise a named MongoDB database
   *
   * This checks to see if the connection is to a [[http://cloudfoundry.org|CloudFoundry]] instance or not.
   * If the connection is local such as in an offline installation or for testing then will connect to the database called '''dbName''' otherwise
   * it will connect to the CF database specified in the connection settings that are provided by CloudFoundry
   *
   * Furthermore, a local connection assumes that MongoDB is running on port 27017 and is accessible via `127.0.0.1`
   *
   * @param dbName The name of the local database to connect to. If connecting to a CloudFoundry database then this parameters is ignored and can be empty.
   *
   */
  def init(dbName: String): Unit = {
    mongoConnectionDetails(dbName) match {
      case Some(x) => x match {
        case config: MongoConnectionConfig => config match {
          case MongoConnectionConfig(host, port, user, pass, db, true) => MongoDB.defineDbAuth(DefaultMongoIdentifier, (host, port), db, user, pass)
          case MongoConnectionConfig(_, _, _, _, db, false) => MongoDB.defineDb(config.dbId, new Mongo, db)
          case _ => error("Failed To initialise mongo DB Connection!")
        }
        case _ => error("Failed To initialise mongo DB Connection!")
      }
      case _ => error("Failed To initialise mongo DB Connection!")
    }
  }

  private implicit def hostToMongo(host: (String, Int)): Mongo = new Mongo(host._1, host._2)

  private def mongoConnectionDetails(dbName: String) = {
    debug("Env: VCAP_SERVICES: %s".format(Option(System.getenv("VCAP_SERVICES"))))

    try {
      Option(System.getenv("VCAP_SERVICES")) match {
        case Some(s) => {
          try {
            debug("We seems to be running on Cloud Foundry. Attempting to extract connection details")
            parse(s) \\ "mongodb-1.8" match {
              case JArray(ary) => ary foreach { mongoJson =>
                val mongo = mongoJson.extract[CloudFoundryMongo]
                val credentials = mongo.credentials
                debug("Extracted CloudFoundry MongoDB: %s\nWith Credentials: %s".format(mongo, credentials))
                Some(MongoConnectionConfig(credentials.hostname, credentials.port.toInt, credentials.username, credentials.password, credentials.db, true))
              }
              case x => warn("Json parse error: %s".format(x))
            }
          }
        }
        case _ => {
          debug("Not running on Cloud Foundry, assuming localhost connection on port 27017")
          Some(MongoConnectionConfig("127.0.0.1", 27017, "", "", dbName, false))
        }
      }
    }
    catch {
      case e: MatchError => {
        error("Match Error: %s\n%s\n%s.\n\nAssuming a service is running locally on port 27017".format(e.getMessage(), e.getCause(), e.getStackTrace().mkString("", "\n", "")))
        None
      }
      case e: Exception => {
        error("Encountered an exception when attempting to initialise connection to MongoDB: %s. Cannot connect!".format(e.getMessage))
        None
      }
    }
  }
}

/**
 * Container for the connection details
 *
 * @constructor Creates a new instance of the connection config
 * @param host The host name or ip to connect to
 * @param port The port to connect to on the host
 * @param user The username to use for authentication
 * @param password The password to use for authentication
 * @param onCloudFoundry A flag to indicate whether or not this is a connection to a CloudFoundry instance.
 *
 * @throws IllegalArgumentException if the `dbName` parameter is empty
 */
case class MongoConnectionConfig(host: String, port: Int, user: String, password: String, dbName: String, onCloudFoundry: Boolean) {
  require(dbName.trim nonEmpty, "DB Name Cannot be empty")

  object dbId extends MongoIdentifier {
    val jndiName = dbName
  }
}
