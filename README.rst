.. image:: https://travis-ci.org/zalando/docker-registry-play.svg
    :target: https://travis-ci.org/zalando/docker-registry-play

====================
Play Docker Registry
====================

This is work in progress.

How to run the web application (you need sbt version 0.13.7):

.. code-block:: bash

    $ sbt ~run

Try to push a Docker image (on another console):

.. code-block:: bash

    $ docker pull busybox
    $ docker tag busybox localhost:9000/test/busybox:1.0
    $ docker push localhost:9000/test/busybox:1.0

Try to pull the same Docker image again (will not do anything as you already have it):

.. code-block:: bash

    $ docker pull localhost:9000/test/busybox:1.0

