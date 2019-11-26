"""
 distutils file to install the python Crest and Http communication wrapper httpio

 The resulting module is called natsio and can be used in python:
       * from crest.io import CrestDbIo

 Henri Louvin - henri.louvin@cea.fr
 Andrea Formica - andrea.formica@cern.ch
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
