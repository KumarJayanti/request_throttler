### Rate Limiting For Requests

A service would like to throttle requests to its resources based on rate limit configuration (e.g requests-per-second).
The rate limit configuration could be at multi-level configuration such as global or per-account, or per-segment level.

 1. Resource level settings override any global settings.
 2. Within any resource/global setting, segment and account level settings can be used to override the top level limit.


This project is a weekend hack project and i shall work on enhancements based on futher interest.

### Getting Started

 1. Clone the repository
 2. Build the maven project
 3. run the unittest AppTest.java


### Prerequisites


```
Give examples
```

### Installing


## Running the tests

## Design
1.  Parse the RateLimit Configurations. Currently, there should be atleast one Global RateLimit Configuration.
    Alternatively we could make the implementation such that when no Global Ratelimit then any resource for which there is no specified limit would NOT BE RATE LIMITED AT ALL.

2.  Create (Resource Configuration Map) a Per-Resource Configuration Tree with Segment and Account Level Limits children.
    This map will only contain entries for resources for which some RateLimit was specified. Any resource for which
    there is no specified RateLimits, the Global Rate Limits Apply.
3.  Runtime :  When a request arrives for a Resource, Lazily compute the Effective RateLimit by Traversing the Configuration
tree for that Resource and Cache a "Per-Account + Per-Resource" RateLimit Entity.  We use a Multi-Level Cache.
     a. The RateLimitsMaster which stores the RateLimit for key=(account:resource)
     b. The RateLimitsCache which fetches a TimeStamped Clone of the RateLimit from the Master and is used to actively decrement the Allowed requests remaining as requests come in.  This Cache is also refreshed if the RateLimit is older than a Second, and access would throw a RequestBlockedException if the number of allowed requests per-second has exceeded.

4.  Makes use of Atomic operations computeIfPresent and computeIfAbsent with the Assumption that the underlying Cache
supplied by DistributedCacheFactory returns Concurrent and Distributed versions of the DataStructure. In the sample implementation
of the Factory a ConcurrentHashMap was used.


### Break down into end to end tests


## Deployment


## Contributing


## Versioning


## Authors


## License


## Acknowledgments


