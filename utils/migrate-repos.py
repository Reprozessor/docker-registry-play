#!/usr/bin/env python
"""
Migrate Docker repositories from one registry to another
"""

import argparse
import requests
import subprocess

def pull(source):
    # find all repos
    r = requests.get('https://' + source + '/v1/search', params={'q': ''}, verify=False)
    data = r.json()

    for result in data['results']:
        print(result)
        # get all tags
        r = requests.get('https://' + source + '/v1/repositories/' + result['name'] + '/tags', verify=False)
        data = r.json()
        for tag in data.keys():
            print(tag)
            subprocess.check_call(['docker', 'pull', source + '/' + result['name'] + ':' + tag])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('source')
    parser.add_argument('destination')

    args = parser.parse_args()

    pull(args.source)
    # push(args.destination)


if __name__ == '__main__':
    main()
