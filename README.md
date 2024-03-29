# panoptes

Panoptes is an experimental investment portfolio compliance assurance engine. Its primary purpose is
to validate assumptions about how such an engine could operate, and to explore a possible
architecture to support realtime processing and cloud deployment.

## Overview

Panoptes is built around the notion of a _portfolio_, which is essentially a collection of
_positions_ (which themselves are holdings of individual _securities_). The owner of a portfolio
typically specifies guidelines which govern its investments; these are translated into a set of
_rules_ which the system can evaluate to determine the portfolio's compliance. For example, a
guideline may stipulate that the portfolio composition may comprise no more than 10% of bonds from a
single issuer, or that the weighted average quality of the portfolio may not fall below 90% of the
rating for a specified benchmark.

A portfolio is not a static entity; its composition may be modified actively (e.g. by trading
activity) or passively (e.g. by market movements affecting the portfolio and/or its benchmark(s),
rating changes, etc.). While Panoptes does not yet implement the notion, it is envisioned that such
changes will be modeled as _events_ to which the system may respond by re-evaluating compliance
rules as necessary. The most significant such event is a proposed _trade_, the compliance impact of
which must be evaluated and feedback returned to the submitting trader (preferably in real time).
