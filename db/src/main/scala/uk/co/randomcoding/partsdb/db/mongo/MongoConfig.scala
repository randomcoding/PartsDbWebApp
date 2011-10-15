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

import com.mongodb.casbah.Imports._

import net.liftweb._
import json._
import common.Loggable

/**
 * Configuration for Mongo DB and access to mongo connections
 *
 * @author RandomCoder
 *
 * Created On: 19 Aug 2011
 *
 */
object MongoConfig extends Loggable {

  private implicit val formats = DefaultFormats

  private var usingCloudfoundry = false
  private var cloudfoundryDbName = ""

  // Case classes required to parse CloudFoundry JSON                                                                                                      
  case class CloudFoundryMongo(name: String, label: String, plan: String, credentials: CloudFoundryMongoCredentials)
  case class CloudFoundryMongoCredentials(hostname: String, port: String, username: String, password: String, name: String, db: String)

  /**
   * Private holder for the MongoDB objects.
   *
   * This will call [[uk.co.randomcoding.partsdb.db.mongo.MongoConfig#init(dbName)]] if there is no database connection for the provided dbName
   */
  private var dbs = Map.empty[String, MongoDB].withDefault(dbName => init(dbName))

  /**
   * Initialise the MongoDB system to create connections etc.
   *
   * This checks to see if the connection is to a [[http://cloudfoundry.org|CloudFoundry]] instance or not.
   * If the connection is local such as in an offline installation or for testing then will connect to the database called '''dbName''' otherwise
   * it will connect to the CF database specified in the connection settings that are provided.
   *
   * Furthermore, a local connection assumes that MongoDB is running on port 27017 and is accessible via `127.0.0.1`
   *
   * @param dbName The name of the local database to connect to. This is also used to cache the generated `MongoDB` instance, so must be specified.
   * @return The [[com.mongobd.casbah.MongoDB]] object that represents the database instance.
   *
   * @throws IllegalArgumentException if the `dbName` parameter is empty
   */
  private def init(dbName: String): MongoDB = {
    require(dbName.nonEmpty, "Parameter dbName cannot be an empty string.\nPlease call init(String) with a dbName.")

    logger.info("Env: VCAP_SERVICES: %s".format(Option(System.getenv("VCAP_SERVICES"))))
    var user = ""
    var port = 27017
    var host = ""
    var pass = ""
    var db = dbName

    try {
      Option(System.getenv("VCAP_SERVICES")) match {
        case Some(s) => {
          try {
            parse(s) \\ "mongodb-1.8" match {
              case JArray(ary) => ary foreach { mongoJson =>
                val mongo = mongoJson.extract[CloudFoundryMongo]
                val credentials = mongo.credentials
                user = credentials.username
                port = credentials.port.toInt
                host = credentials.hostname
                pass = credentials.password
                db = credentials.db
                cloudfoundryDbName = db
                usingCloudfoundry = true
              }
              case x => logger.warn("Json parse error: %s".format(x))
            }
          }
        }
        case _ => logger.info("Not running on Cloud Foundry, assuming localhost connection on port 27017")
      }
    }
    catch {
      case e: MatchError => logger.error("Match Error: %s\n%s\n%s.\n\nAssuming a service is running locally on port 27017".format(e.getMessage(), e.getCause(), e.getStackTrace().mkString("", "\n", "")))
    }

    val connection = host match {
      case "" => MongoConnection()
      case hostName => MongoConnection(hostName, port)
    }

    val mongoDb = connection(db)

    if (user.nonEmpty) {
      mongoDb.authenticate(user, pass)
    }

    dbs = dbs + (db -> mongoDb)
    mongoDb
  }

  /**
   * Get or create a named collection in the specified database.
   *
   * This gets the specified `MongoDB` instance from the private cache, which in turn will create the connection if is is not present.
   *
   * @param dbName The name of the database to get the collections from.
   * @param collectionId The name of the collection to get from the database. If this does not exist, then `MongoDB` will create it.
   */
  def getCollection(dbName: String, collectionId: String): MongoCollection = {
    val conn = usingCloudfoundry match {
      case true => dbs(cloudfoundryDbName)
      case false => dbs(dbName)
    }
    logger.debug("Getting collection %s from %s".format(collectionId, conn))

    conn(collectionId)
  }
}
