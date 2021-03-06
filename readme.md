utree - Utility Trees
==========

A utility tree is a picture that helps articular architectural
qualities for a particular system. When faced with general terms like
"scalability", "security", "performance", "modifiability", and so on,
a customer's only possible response is, "Yes, please. I'd like some of
those." It doesn't help to ask which one is more important. The
problem is that those terms are too abstract.

A utility tree lets you get down to brass tacks, by talking about
concrete "quality scenarios".  A single quality scenario will be
attached to a facet of a quality. It should be measurable and
quantitative.


To learn more about utility trees, take a look at ["Evaluting Software
Architectures"](http://www.amazon.com/gp/product/020170482X/ref=as_li_tl?ie=UTF8&camp=1789&creative=390957&creativeASIN=020170482X&linkCode=as2&tag=wwwmichaelnyg-20&linkId=Q2IV6QM3BIYNDOH4), by Clements, Kazman, and Klein.

Building
----------

This uses [Leiningen](https://github.com/technomancy/leiningen/wiki/Upgrading) to build. Run
all of these from the base of the project.

To build a standalone jar file:

    lein jar

Usage
----------
Run from the command line:

    java -cp `lein classpath` utree.core _command_ _command-options_

Alternatively, you can build a standalone jar and run that directly:
    lein uberjar
    java -cp target/utree-0.0.4-standalone.jar _command_ _command-options_

Subcommands are:

* dot - Turn a utility tree into a [dot graph](http://www.graphviz.org) for making images.
* radar-plots - Render radar plots as PNGs for the solution alternatives in the input file.

Command: dot
---------------

    java -cp `lein classpath` utree.core dot _filename_

Use _filename_ as the input file (format below).

Example:

    java -cp `lein classpath` utree.core dot sample.ut | dot -Tpng -osample.png

Command: radar-plots
--------------------

Use _filename_ as the input file. Render PNGs named `solution_0.png`
through `solution_n.png`.

Example:

    java -cp target/utree-0.0.4-standlone.jar radar-plots example/alternatives.ut

This will create solution\_0.png through solution\_3.png, showing how
the different alternative solutions score on the axes that matter.

Input Format
------------

Pictures don't version control well. I wanted a text-based format that
would work well with all our other project artifacts.

The input format is a text file, consisting of one or more sections.
Each section is marked by a header: a line starting with 2 or more
hypens and a section type.

Example:

    -- Utility
    # Quality attribute lines follow

    -- Alternatives
    # Solution alternatives follow


Quality attribute lines start with asterisks. These will appear in the
output diagram. The number of lines indicates that attribute's nesting
level. There is no line-wrapping.

Example:

    * Availability
    ** COTS Software Failures
    ** Hardware Failure
    *** Power outage at site 1 requires traffic redirect to site 3 in < 3 seconds.
    *** Restart after disk failure in < 5 minutes.
    *** Network failure is detected and recovered in < 1.5 minutes.

If you want to add comments, version control IDs, or extra text, feel
free. Other lines will be silently ignored.

Ranking and Weighting
---------------------

You can add explicit ranking within a group of quality attributes by
adding a number inside of square brackets. These ranks eventually get
turned into relative weights for each quality attribute.

Example:

    * [1] Availability
    ** [1] Hardware Failures
    *** [2] Power outage at site 1 requires traffic redirect to site 3 in < 3 seconds.
    *** [3] Restart after disk failure in < 5 minutes.
    *** [1] Network failure is detected and recovered in < 1.5 minutes.
    ** [1] COTS Software Failures
    * [2] Security
    ** [1] Confidentiality
    ** [2] Integrity

The reason for adding ranks explicitly, rather than just computing
them based on the sequence is so you can capture the scenarios in one
pass, then come back and rank them in a second pass, without having to
shuffle lines and sections around. (Yes, I know that Emacs outline
mode makes this easy. I use it, but I'm not going to force it on
everyone. That would be cruel... some people just aren't cut out for
Emacs.)

The ranking at each level influences ranking of the child elements. In
the example above, "Hardware Failures" is ranked first of two, so it
gets a weight of 0.75. Security is second of two, so it gets
0.25. Then within the hardware failures attribute, we have 3
scenarios. The top ranked item "Network failure..." gets a local
weight of 0.61111... times its inherited weight of 0.75 for an overall
weight of 0.458333... The second ranked item has a local weight of
0.2777... times its inherited weight of 0.75 for an overall weight of
0.2083...

These weights can then be used in a decision matrix to evaluate
alternative solutions. When considering several architectures for a
system, you can rate each architecture relative to how well it
supports the quality scenarios. Multiply the ratings by the weights
computed here, and you'll have a weighted score for each alternative.

See [James McCaffrey's blog
post](http://jamesmccaffrey.wordpress.com/2006/09/28/rank-order-centroids-in-testing/)
about computing rank-ordered centroids. See also dot-utility.graph/roc
for an implementation.

Rank-ordered centroids are used in LAAAM (full support coming soon
here!), which is an architecture-focused instantiation of the
[Analytic Hierarchy
Process](http://en.wikipedia.org/wiki/Analytic_Hierarchy_Process).

At this time, weights only appear as annotations on the quality
scenarios. Watch this space for more.
