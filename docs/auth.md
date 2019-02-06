# Auth

Functions prefixed with `auth:` like `db/auth:put` needs to be based to
additional arguments, `user` and `permissions`, to check the authorization of
the function call. Functions that gets called by `auth:` prefixed functions,
should be private to avoid being called without `user` and `permissions`.

## Authentication

Each user is a map with `username`, `password`, `roles` and `permissions` keys.

Password is using the `bcrypt` password hashing function.

Roles is currently limited to one `role` but setup that way to make it easier
if needed to have multiple in the future.

Notice: `roles` VS `groups` naming still undecided.

### HTTP

Authentication for the HTTP endpoints works the same way for all endpoints
except the `/login` handler.

`/login` handler checks the request body for a username and password, checks
it's correct and if so, signs the user's username with the applications secret.
This JWT token is then used for doing the authentication and authorization.

The rest of the endpoints protected by authentication, are checking for the
token in the `Authorization` header, following a `Token ` declaration.

### WebSockets

When the client (browser) connects to the WebSocket endpoint, it expects a cookie
named `login-token` with the value of received JWT signed token from the login.

If the cookie is missing or the JWT is incorrect, the connection will fail.

If the authentication is correct, then the username signed in the JWT will be
used to do the filtering of the DB so the user only sees the parts of the DB
they are authorized to see.

All writes to the DB is handled via HTTP so the authentication described above
applies.

## Authorization

Authorization is handled via the abstractions of `users`, `roles` and `permissions`

Users are described above in the Authentication part.

## Groups

A group is a named list of N permissions. Instead of assigning individual
permissions to a user, owners of a Cube instance assigns permissions to groups,
then assigns users to groups, to make reuse of permissions easier.

## Permissions

A permission is a vector of two keywords, the first one describing what part
of the DB this permission references, the second one is either `:read` or `:write`.

For example, the permission `[:pins :read]` indicates that this group has read
access to the pins of Cube.

This permissions are currently being used to protect both the backend<>frontend
synced DB and the HTTP endpoints (except `/login`, anyone can hit that endpoint)

# TODO
- [ ] Create spec for permissions/groups/users
- [ ] Protect write endpoints
- [ ] Allow to CRUD users
- [ ] Allow to CRUD groups
- [ ] Generate new `admin` password if first time running Cube
- [ ] Enable changing the JWT signing secret via env vars
