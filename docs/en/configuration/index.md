---
title: Configuration
---
The SCM-Commit-Message-Checker-Plugin can be configured globally and repository specific. The global configuration is used for all repositories which doesn't have a specific config. The repository specific configuration can be disabled in the global config.

### Configuration form
To validate your commit messages you can use the provided configurable validators. Other SCM-Manager plugins may provide their own validators which can be used in this configuration.
This plugin only has one validator which can be applied multiple times.

### Custom RegEx Validator
The "Custom Regular Expression Validator" can validate commit messages using your own regular expression. 
Besides the pattern you can also set which branches should be validated and what error message will be shown on invalid commit messages.

The validator will check, if the pattern matches any part of the message. So if you want the whole message to be checked,
you have to enclose the pattern with `^` and `$` like `^[A-Za-z0-9 ]*$`.

Here are some examples:

| Expression              |Check|
|-------------------------|---|
| `^[A-Za-z0-9 ]*$`       |The complete message must only contain letters, numbers and spaces|
| `#[0-9]+`               |The message has to contain a number with a leading hash like `#42`|
| `.{10}`                 |The message has to have at least 10 characters|
| `fixed\|updated\|added` |The message has to contain one of the words `fixed`, `updated`, or `added`|

![Commit Message Validation configuration](assets/config.png)
