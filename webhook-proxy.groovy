import io.vertx.core.json.JsonObject;
import io.vertx.groovy.core.buffer.Buffer

def serverPort = 9090 // TODO: param this
def serverHost = 'localhost' // TODO: param this
def targetHost = '' // TODO: param this

def client = vertx.createHttpClient()

def server = vertx.createHttpServer().requestHandler({ req ->
	println "Receiving request: ${req.uri()}"

	// Don't work, receive 404 and don't kown why (bad host?). Broken since using vertx3
	/*
	def c_req = client.request(req.method(), targetHost, req.uri(), { c_res ->
		println "Proxying response: ${c_res.statusCode()}"
		//req.response().chunked = true
		req.response().statusCode = c_res.statusCode()
		req.response().headers().setAll(c_res.headers())
		c_res.bodyHandler { resBuffer ->
			println "Proxying response body: $resBuffer"
			req.response().end(resBuffer)
		}
	})

	//c_req.chunked = true
	c_req.headers().setAll(req.headers())
	*/

	req.bodyHandler({ totalBuffer ->
		// Now all the body has been read
		def res = totalBuffer.toJsonObject()
		println "request body: $res"

		def finalCommits = res.commits.findAll({ commit ->
			// TODO: filter on param author regexp
			!commit.message.startsWith('[jenkins-release] ') // TODO: use a param message regexp
		})
		//println "finalCommits: ${finalCommits}"

		if (finalCommits.size() > 0) {
			// Don't work, receive 404 and don't kown why (bad host?). Broken since using vertx3
			// c_req.end(totalBuffer)
			// Do a simple get without proxying original request methods, data, ...
			client.getNow(targetHost, req.uri(), { response ->
				println("Received response with status code ${response.statusCode()}")
				response.bodyHandler { resBuffer ->
					println "response body: $resBuffer"
					req.response().end(resBuffer)
				}
			})

		} else {
			println('Request filtered')
			req.response().end()
		}
	})

}).listen(serverPort, serverHost)
