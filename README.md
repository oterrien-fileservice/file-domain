# file-domain

The goal of this hexagon is to implement business logic for reading/writing remote files.
User's read/write authorization is checked before being applied.

As a user, I want to read a file which is stored remotely and depending on application
As a user, I want to write a file (or to create it if not exist) which is stored remotely and depending on application

## Manage conflicts

When 2 users request to read the same file at the same time, a lock is put for both requests (for the same file) but is not blocking for the reading
When 2 users request to write the same file at the same time, the first acquires the lock on the file and can write it, but the second request is rejected
When 1 user requests to read a file which is being written (locked) by another request, then its request is rejected
When 1 user requests to write a file which is being downloaded (locked) by another request, then its request is rejected
