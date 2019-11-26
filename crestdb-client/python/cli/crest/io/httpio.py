"""
 HTTP client tool for Svom

 Retries requests with power law delays and a max tries limit

 @author: henri.louvin@cea.fr
"""

import threading

import time

import requests

import asyncio
import aiohttp
import signal

# Log
import logging
log = logging.getLogger('http_client')

class HttpClient(threading.Thread):
    """
    Threaded asyncio NATS client
    """
    def __init__(self, _http_client, loop):
        """Init asyncio thread"""
        threading.Thread.__init__(self)
        self._http_client = _http_client
        self.loop = loop

    # called by thread.start()
    def run(self):
        """Start asyncio loop"""
        if not self.loop.is_running():
            log.debug("Starting async event loop")
            self.loop.run_forever()


class HttpIo:
    """
    (A)synchronous HTTP client
    """
    def __init__(self, server_url, max_tries=5, backoff_factor=1, asynchronous=False, loop=None): #pylint: disable=R0913
        self.server_url = server_url.strip('/')
        self.max_tries = max_tries
        self.backoff_factor = backoff_factor
        self.loop = None
        self.async_session = None
        self.asynchronous = asynchronous
        if asynchronous is True:
            self.loop = loop if loop is not None else asyncio.get_event_loop()
            self.async_session = self._get_client(self.loop)
            signal.signal(signal.SIGINT, self.sigint_handler)
            async_client = HttpClient(self.async_session, self.loop)
            async_client.start()

    def _get_client(self, loop):
        return aiohttp.ClientSession(loop=loop)

    def _compute_delay(self, tried):
        """ calculate delay in seconds as a power law """
        delay = self.backoff_factor * (2 ** (tried-1))
        return delay

    def _sync_request(self, req, url, **kwargs):
        """ Synchronous {req} request to {url} with data {kwargs}"""
        tried = 0
        response = None
        success = False
        log.info('%s request to %s: %s', req, url, kwargs)
        while success is False:
            if req == 'GET':
                response = requests.get(url, **kwargs)
            elif req == 'POST':
                response = requests.post(url, **kwargs)
            else:
                exc = f"_sync_request() only handles 'GET' and 'POST' requests, not '{req}'"
                raise ValueError(exc)
            tried += 1
            # retrieve response.json()
            json_resp = {}
            try:
                json_resp = response.json()
            except Exception:
                pass
            log.debug('Server %s response (%s): %s', req, response.status_code, json_resp)
            # accept all responses with status 2xx
            if int(response.status_code/100) == 2:
                log.info('%s request to %s successfull (%s)', req, url, response.status_code)
                success = True
            elif tried < self.max_tries:
                delay = self._compute_delay(tried)
                log.warning('%s request to %s failed (%s). Trying again in %ss',
                            req, url, response.status_code, delay)
                time.sleep(delay)
                log.debug('%s request (%sth try) to %s: %s', req, tried+1, url, kwargs)
            else:
                log.warning('%s request to %s failed (%s)', req, url, response.status_code)
                log.error('%s request to %s failed %s times. Aborting', req, url, self.max_tries)
                break
        # override response.json()
        response.json = lambda: json_resp
        return response

    async def _async_request(self, req, url, tried, **kwargs):
        """ Asynchronous request to {url} with data {kwatgs}"""
        # delay request if needed
        if tried > 0:
            delay = self._compute_delay(tried)
            await asyncio.sleep(delay)
            log.debug('%s request (async, %sth try) to %s: %s', req, tried+1, url, kwargs)
        else:
            log.info('%s request (async) to %s: %s', req, url, kwargs)
        tried += 1
        try:
            response = None
            if req == 'GET':
                response = await self.async_session.get(url, **kwargs)
            elif req == 'POST':
                response = await self.async_session.post(url, **kwargs)
            else:
                exc = f"_async_request() only handles 'GET' and 'POST' requests, not '{req}'"
                raise ValueError(exc)
            # retrieve response.json()
            json_resp = {}
            try:
                json_resp = await response.json()
            except Exception:
                pass
            log.debug('Server %s response (%s): %s', req, response.status, json_resp)
            # accept all responses with status 2xx
            if int(response.status/100) == 2:
                log.info('%s request (async) to %s successfull (%s)', req, url, response.status)
                # override response.json()
                response.json = lambda: json_resp
                response.status_code = response.status
            elif tried < self.max_tries:
                delay = self._compute_delay(tried)
                log.warning('%s request (async) to %s failed (%s). Trying again in %ss',
                            req, url, response.status, delay)
                response = await self._async_request(req, url, tried, **kwargs)
            else:
                log.warning('%s request (async) to %s failed (%s)', req, url, response.status)
                log.error('%s request (async) to %s failed %s times. Aborting',
                          req, url, self.max_tries)
                # override response.json()
                response.json = lambda: json_resp
                response.status_code = response.status
        except Exception as exc:
            log.error('Exception caught: %s', exc)
            delay = self._compute_delay(tried)
            log.error('%s request (async) to %s failed. Aborting', req, url)
            raise
        return response

    def get(self, endpoint='/', **kwargs):
        """ GET request to endpoint {endpoint} with json data {data}"""
        url = f"{self.server_url}/{endpoint.strip('/')}"
        return self._sync_request('GET', url, **kwargs)

    def post(self, endpoint='/', **kwargs):
        """ POST request to endpoint {endpoint} with json data {data}"""
        url = f"{self.server_url}/{endpoint.strip('/')}"
        return self._sync_request('POST', url, **kwargs)

    async def async_get(self, endpoint='/', **kwargs):
        """ GET request to endpoint {endpoint} with json data {data}"""
        url = f"{self.server_url}/{endpoint.strip('/')}"
        return await self._async_request('GET', url, tried=0, **kwargs)

    async def async_post(self, endpoint='/', **kwargs):
        """ POST request to endpoint {endpoint} with json data {data}"""
        url = f"{self.server_url}/{endpoint.strip('/')}"
        return await self._async_request('POST', url, tried=0, **kwargs)

    def stop(self):
        """ close http session if asynchronous """
        if self.asynchronous is False:
            return
        log.debug('Stopping async client...')
        log.debug('Cancelling pending tasks...')
        # Cancel pending tasks
        for task in asyncio.Task.all_tasks():
            task.cancel()
        # Stop HTTP session properly
        future = asyncio.run_coroutine_threadsafe(self.async_session.close(),
                                                  loop=self.loop)
        # Wait for HTTP session to close then stop async loop
        try:
            log.debug('Waiting for aiohttp.ClientSession to close...')
            future.result()
            log.debug('Done. Stopping async event loop')
            self.loop.call_soon_threadsafe(self.loop.stop)
        except Exception as err:
            log.error(err)

    def sigint_handler(self, signum, frame):
        """
        stops gracefully, restore default signal handling
        and raises KeyboardInterrupt
        """
        self.stop()
        signal.signal(signal.SIGINT, signal.SIG_DFL)
        raise KeyboardInterrupt
