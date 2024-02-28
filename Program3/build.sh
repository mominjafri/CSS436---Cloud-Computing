# Outputting the initial message
echo "THIS IS NOT A VALID SCRIPT, PLEASE SEE INSTRUCTIONS BELOW:"
echo

# Instructions for backup.py
echo "For backup.py, please run the following command:"
echo "python3 backup.py backup directory-name bucket-name::directory-name"
echo "Where directory-name is a directory on your computer, bucket-name is a bucket in your S3 account, and directory-name is a directory in the bucket."
echo

# Instructions for restore.py
echo "For restore.py, please run the following command:"
echo "python3 restore.py bucket-directory bucket-name::directory-name"
echo "Where bucket-directory is a directory in your S3 account, bucket-name is a bucket in your S3 account, and directory-name is a directory on your computer."
echo

echo "Depending on which version of python you are using, you may need to run the above commands with 'python' instead of 'python3'."