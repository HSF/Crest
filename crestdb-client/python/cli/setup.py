"""
 distutils file to install the python NATS communication wrapper natsio

 The resulting module is called natsio and can be used in python:
       * from svom.messaging.natsio import NatsIo

 Henri Louvin - henri.louvin@cea.fr
"""
from setuptools import setup, find_namespace_packages

setup(name='crest.io',
      version='1.0',
      author="Henri Louvin",
      description="""Python module wrapping HttpIo for crest use """,
      python_requires=">=3.5",
      namespace_packages=['crest'],
      packages=find_namespace_packages(include=['crest.*']),
      install_requires=['asyncio',
                        'requests',
                        'aiohttp'],
     )
