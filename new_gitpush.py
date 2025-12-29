#!/data/data/com.termux/files/usr/bin/python3

import os
import sys
import time
import subprocess
import getpass
from datetime import datetime

def print_with_delay(message, delay=1.5):
    """Print message with specified delay"""
    print(f"\n[ {datetime.now().strftime('%H:%M:%S')} ] {message}")
    time.sleep(delay)

def run_command(cmd, description):
    """Execute shell command with error handling"""
    print_with_delay(f"📦 {description}...", 0.5)
    try:
        result = subprocess.run(cmd, shell=True, capture_output=True, text=True, check=True)
        print(f"   ✅ Success: {description}")
        if result.stdout.strip():
            print(f"   📋 Output: {result.stdout.strip()}")
        return True
    except subprocess.CalledProcessError as e:
        print(f"   ❌ Failed: {description}")
        print(f"   💬 Error: {e.stderr.strip() if e.stderr else e.stdout.strip()}")
        return False

def check_gh_installed():
    """Check if GitHub CLI is installed"""
    print_with_delay("🔍 Checking for GitHub CLI...", 0.5)
    try:
        subprocess.run(["gh", "--version"], capture_output=True, check=True)
        print("   ✅ GitHub CLI is installed")
        return True
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("   ❌ GitHub CLI not found!")
        print("   💡 Install with: pkg install gh")
        return False

def check_gh_auth():
    """Check if user is authenticated with GitHub"""
    print_with_delay("🔐 Checking GitHub authentication...", 0.5)
    try:
        result = subprocess.run(["gh", "auth", "status"], capture_output=True, text=True)
        if "Logged in to github.com" in result.stdout:
            print("   ✅ GitHub authentication verified")
            return True
        else:
            print("   ❌ Not authenticated with GitHub")
            return False
    except subprocess.CalledProcessError:
        return False

def setup_github_repo():
    """Initialize Git repository and create GitHub repo if needed"""
    current_dir = os.getcwd()
    dir_name = os.path.basename(current_dir)
    
    print_with_delay(f"📁 Current directory: {current_dir}", 1)
    print_with_delay(f"📂 Directory name: {dir_name}", 1)
    
    # Check if directory is already a git repo
    if os.path.exists(".git"):
        print("   ✅ Git repository already initialized")
        return True
    
    # Ask for confirmation
    print(f"\n{'='*60}")
    print(f"⚠️  WARNING: You are about to push directory:")
    print(f"📂 '{dir_name}'")
    print(f"📁 From: {current_dir}")
    print(f"{'='*60}")
    
    response = input("\nPress Enter to continue or 'n' to cancel: ").strip().lower()
    if response == 'n':
        print_with_delay("🚫 Operation cancelled by user", 1)
        sys.exit(0)
    
    # Initialize git
    if not run_command("git init", "Initializing Git repository"):
        return False
    
    print_with_delay("📝 Creating .gitignore file...", 1)
    gitignore_content = """# Python
__pycache__/
*.py[cod]
*$py.class
*.so
.Python
env/
venv/
ENV/
env.bak/
venv.bak/

# IDE
.vscode/
.idea/
*.swp
*.swo

# OS
.DS_Store
.DS_Store?
._*
.Spotlight-V100
.Trashes
ehthumbs.db
Thumbs.db

# Termux
termux/
.cache/
"""
    
    with open(".gitignore", "w") as f:
        f.write(gitignore_content)
    print("   ✅ Created .gitignore file")
    
    # Check if GitHub repo exists
    print_with_delay("🌐 Checking if GitHub repository exists...", 1)
    try:
        result = subprocess.run(
            ["gh", "repo", "view", dir_name],
            capture_output=True, text=True
        )
        if result.returncode == 0:
            print("   ✅ GitHub repository exists")
            remote_url = f"https://github.com/{getpass.getuser()}/{dir_name}.git"
            run_command(f"git remote add origin {remote_url}", "Adding remote repository")
        else:
            print("   📝 Creating new GitHub repository...")
            if run_command(f"gh repo create {dir_name} --public --source=. --remote=origin --push", 
                          "Creating GitHub repository"):
                print_with_delay("🎉 GitHub repository created successfully!", 2)
                return True
    except Exception as e:
        print(f"   ❌ Error checking GitHub repo: {e}")
    
    return True

def commit_and_push():
    """Commit changes and push to GitHub"""
    # Add all files
    if not run_command("git add .", "Staging all files"):
        return False
    
    # Commit with timestamp
    commit_msg = f"Auto-commit: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"
    if not run_command(f'git commit -m "{commit_msg}"', "Committing changes"):
        # If commit fails (no changes), create an empty commit
        print_with_delay("No changes to commit, creating empty commit...", 1)
        run_command('git commit --allow-empty -m "Empty commit: No changes"', "Creating empty commit")
    
    # Push to GitHub
    print_with_delay("🚀 Pushing to GitHub...", 2)
    if run_command("git push -u origin main", "Pushing to main branch"):
        print_with_delay("✨ Successfully pushed to main branch!", 2)
        return True
    else:
        # Try pushing to master if main fails
        print_with_delay("Trying master branch instead...", 1)
        if run_command("git push -u origin master", "Pushing to master branch"):
            print_with_delay("✨ Successfully pushed to master branch!", 2)
            return True
    
    return False

def main():
    """Main automation function"""
    print("\n" + "="*60)
    print("🚀 GITHUB AUTO-PUSH SCRIPT FOR TERMUX")
    print("="*60)
    
    # Check prerequisites
    if not check_gh_installed():
        sys.exit(1)
    
    if not check_gh_auth():
        print_with_delay("🔐 Please authenticate with GitHub first:", 1)
        print("   Run: gh auth login")
        print("   Follow the prompts to authenticate")
        sys.exit(1)
    
    # Setup repository
    print_with_delay("⚙️  Setting up repository...", 1)
    if not setup_github_repo():
        print_with_delay("❌ Failed to setup repository", 2)
        sys.exit(1)
    
    # Commit and push
    print_with_delay("📤 Starting push process...", 2)
    if commit_and_push():
        # Get remote URL
        try:
            result = subprocess.run(
                ["git", "remote", "get-url", "origin"],
                capture_output=True, text=True, check=True
            )
            repo_url = result.stdout.strip()
            print("\n" + "="*60)
            print("✅ SUCCESS!")
            print(f"📁 Repository: {os.path.basename(os.getcwd())}")
            print(f"🌐 URL: {repo_url}")
            print("="*60)
        except:
            print("\n✅ Project pushed successfully!")
    else:
        print_with_delay("❌ Failed to push to GitHub", 2)
        sys.exit(1)
    
    print_with_delay("👋 Script completed!", 1)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n🚫 Operation cancelled by user")
        sys.exit(0)
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
        sys.exit(1)