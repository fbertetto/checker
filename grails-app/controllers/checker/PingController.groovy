package checker

import grails.rest.RestfulController

/**
 * This class is just to test correct functionality of the server.
 * 
 * @author fabianbertetto
 *
 */
class PingController extends RestfulController {
	def ping = { render "pong" }
}

