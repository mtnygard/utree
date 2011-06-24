dot-utility
==========

Convert simple text format to a [dot graph](http://www.graphviz.org)
for making utility trees.

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
Architectures"](http://www.amazon.com/gp/product/020170482X/ref=as_li_ss_tl?ie=UTF8&tag=michaelnygard-20&linkCode=as2&camp=217145&creative=399369&creativeASIN=020170482X), by Clements, Kazman, and Klein.

Building
----------
This uses [cake](http://github.com/ninjudd/cake) to build. Run:
    cake bin

to create a standalone executable for your system.

Usage
----------
Run from the command line:
    dot-utility _inputfile_

Output goes to standard out. You can redirect it wherever you like.


Input Format
----------

Picture don't version control well. I wanted a text-based format that
would work well with all our other project artifacts.

The input format is a text file. Lines starting with asterisks will
appear in the output diagram. Everything else is ignored. There is no
line-wrapping.

Example:

    * Availability
    ** COTS Software Failures
    ** Hardware Failure
    *** (L, H) Power outage at site 1 requires traffic redirect to site 3 in < 3 seconds.
    *** (M, M) Restart after disk failure in < 5 minutes.
    *** (H, M) Network failure is detected and recovered in < 1.5 minutes.

If you want to add comments, version control IDs, or extra text, feel
free. Other lines will be silently ignored.

Output Format
----------

The output is a dot digraph, suitable for rendering however you like.

Example:

    dot -Tpng -osample.png sample.dot

Or, if you like shell-fu

    dot-utility sample.ut | dot -Tpng -osample.png


