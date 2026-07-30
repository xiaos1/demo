"""
Example: read a value the client types on the command line as an input argument.

Usage:
    python3 scripts/cli_input_example.py "hello world"
    python3 scripts/cli_input_example.py --message "hello world"
"""

import argparse
import sys


def read_with_sys_argv():
    """Simplest approach: positional args are just a list in sys.argv."""
    if len(sys.argv) < 2:
        print("Usage: python3 cli_input_example.py <your text>")
        sys.exit(1)
    return sys.argv[1]


def read_with_argparse():
    """More flexible approach: named/optional flags, help text, validation."""
    parser = argparse.ArgumentParser(description="Read a string typed on the command line.")
    parser.add_argument(
        "-m", "--message",
        type=str,
        required=True,
        help="the text to pass in from the command line",
    )
    args = parser.parse_args()
    return args.message


def main():
    if "--message" in sys.argv or "-m" in sys.argv:
        text = read_with_argparse()
    else:
        text = read_with_sys_argv()

    print(f"You entered: {text}")


if __name__ == "__main__":
    main()
