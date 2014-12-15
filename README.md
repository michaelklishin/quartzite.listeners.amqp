# What is quartzite.listeners.amqp

This is a collection of Quartz listeners that publish events serialized in JSON over AMQP. This Quartzite extension
is intended primarily to be used in Clojure applications that use [Quartzite](https://github.com/michaelklishin/quartzite).

It uses [Langohr](https://github.com/michaelklishin/langohr) as [AMQP 0.9.1](http://bit.ly/amqp-model-explained) client
and [clojure.data.json](https://github.com/clojure/data.json) for message payload serialization.


## Usage

Quartzite AMQP Listeners is a young project and until 1.0 is released and documentation guides are written,
it may be challenging to use for anyone except the author.

Once the library matures, we will update this document.


## Supported Clojure versions

[Quartzite](https://github.com/michaelklishin/quartzite) and its extensions were built from the ground up for Clojure 1.3 and up.
Quartzite extensions like AMQP listeners support the same Clojure versions.


## Maven Artifacts

With Leiningen:

    [clojurewerkz/quartzite.listeners.amqp "1.0.0-SNAPSHOT"]

New snapshots are released to [clojars.org](https://clojars.org/clojurewerkz/quartzite.listeners.amqp) every few days.


## Continuous Integration

[![Continuous Integration status](https://secure.travis-ci.org/michaelklishin/quartzite.listeners.amqp.png)](http://travis-ci.org/michaelklishin/quartzite.listeners.amqp)

CI is hosted by [travis-ci.org](http://travis-ci.org)


## Development

Quartzite AMQP Listeners uses [Leiningen 2](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md). Make
sure you have it installed and then run tests against all supported Clojure versions using

    lein all test

Then create a branch and make your changes on it. Once you are done with your changes and all
tests pass, submit a pull request on Github.


## License

Copyright (C) 2012 Michael S. Klishin

Distributed under the Eclipse Public License, the same as Clojure.
